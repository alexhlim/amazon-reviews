package com.amazonreviews.streaming

import com.audienceproject.spark.dynamodb.implicits._
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.http.apache.ApacheHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.kinesis.KinesisClient
import software.amazon.awssdk.services.kinesis.model.{DescribeStreamRequest}
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Milliseconds, Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.kinesis.{
  KinesisInitialPositions,
  KinesisInputDStream
}
import org.json4s._
import org.json4s.jackson.JsonMethods._

object KinesisSparkStreamDynamo {

  def getNumShards(kinesisClient: KinesisClient, streamName: String): Int = {
    val describeStreamRequest =
      DescribeStreamRequest.builder().streamName(streamName).build()
    val numShards = kinesisClient
      .describeStream(describeStreamRequest)
      .streamDescription()
      .shards()
      .size
    numShards
  }
  // sbt run AmazonReviews AmazonReviews-APIData https://kinesis.us-east-2.amazonaws.com AmazonReviews-Product
  def main(args: Array[String]) {
    val Array(appName, streamName, endpointUrl, tableName) = args

    val conf = new SparkConf().setMaster("local[2]").setAppName(appName)
    val ssc = new StreamingContext(conf, Seconds(1))
    val sc = ssc.sparkContext
    sc.setLogLevel("ERROR")
    println(s"Launching: ${appName}")

    val credentials = DefaultCredentialsProvider.create().resolveCredentials()
    require(
      credentials != null,
      """""
      No AWS credentials found. Please provide using one of methods described in
      http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html
      """"
    )
    val region = Region.US_EAST_2

    println(s"AWS Access Key: ${credentials.accessKeyId()}")
    println(s"Region currently being used: $region")

    val kinesisClient = KinesisClient
      .builder()
      .httpClient(ApacheHttpClient.create())
      .region(region)
      .build()
    val numShards = getNumShards(kinesisClient, streamName)
    val numStreams = numShards
    println(s"Number of shards/streams: $numStreams")
    val checkpointInterval = Milliseconds(100)
    // Each Kinesis shard becomes its own input DStream
    val kinesisStreams = (0 until numStreams).map { i =>
      KinesisInputDStream.builder
        .streamingContext(ssc)
        .endpointUrl(endpointUrl)
        .regionName(region.toString())
        .streamName(streamName)
        .initialPosition(new KinesisInitialPositions.Latest())
        .checkpointAppName(appName)
        .checkpointInterval(checkpointInterval)
        .storageLevel(StorageLevel.MEMORY_AND_DISK_2)
        .build()
    }

    // Unifying input DStreams
    val unionStreams = ssc.union(kinesisStreams)

    // Transforming elements in DStream to ProductReviewData schema
    val inputStreamData = unionStreams.map { byteArray =>
      val jsonString = new String(byteArray)
      val json = parse(jsonString)
      val id = compact(render(json \ "id"))
      val reviewId = compact(render(json \ "reviews.id"))
      // Removing ids from JSON
      val reviewJson = compact(render(json removeField {
        case JField("id", _)         => true
        case JField("reviews.id", _) => true
        case _                       => false
      }))
      ProductReviewData(id, reviewId, reviewJson)
    }

    val inputStream: DStream[ProductReviewData] = inputStreamData

    inputStream.window(Seconds(10)).foreachRDD { rdd =>
      val spark = SparkSession.builder().getOrCreate()
      // Spark needs to be initalized before importing this, needed for .toDF()
      import spark.implicits._
      val reviewDf = rdd.toDF()
      reviewDf.show(1)
      reviewDf.write.option("region", region.toString()).dynamodb(tableName)
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
case class ProductReviewData(id: String, reviewId: String, reviewJson: String)
