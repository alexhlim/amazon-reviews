package com.amazonreviews.streaming

import com.amazonreviews.streaming.KinesisSparkHelper.{
  getNumShards,
  getCredentials,
  getRegion,
  processAPIDataDynamo
}
import com.audienceproject.spark.dynamodb.implicits._
import software.amazon.awssdk.http.apache.ApacheHttpClient
import software.amazon.awssdk.services.kinesis.KinesisClient
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Milliseconds, Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kinesis.{
  KinesisInitialPositions,
  KinesisInputDStream
}

object KinesisSparkStreamDynamo {

  def getInputStream(
      unionStreams: DStream[Array[Byte]]
  ): DStream[ProductReviewDynamo] = {
    val inputStreamData = unionStreams.map { byteArray =>
      val jsonString = new String(byteArray)
      processAPIDataDynamo(jsonString)
    }
    inputStreamData
  }

  def writeToDynamodb(
      inputStream: DStream[ProductReviewDynamo],
      region: String,
      tableName: String
  ): Unit = {
    // Process only current batch
    inputStream.foreachRDD { rdd =>
      val spark = SparkSession.builder().getOrCreate()
      // Spark needs to be initalized before importing this, needed for .toDF()
      import spark.implicits._
      val reviewDf = rdd.toDF()
      if (!reviewDf.isEmpty) {
        val dynamoDf =
          reviewDf.groupBy("id").pivot("reviewId").agg(first("reviewJson"))
        dynamoDf.show(1)
        dynamoDf.write.option("region", region.toString()).dynamodb(tableName)
      }

    }
  }

  def writeToRedshift(inputStream: DStream[ProductReviewDynamo]): Unit = {}

  def main(args: Array[String]) {
    val Array(appName, streamName, endpointUrl, tableName) = args

    val conf = new SparkConf().setMaster("local[2]").setAppName(appName)
    val ssc = new StreamingContext(conf, Seconds(1))
    val sc = ssc.sparkContext
    sc.setLogLevel("ERROR")
    println(s"Launching: ${appName}")

    val credentials = getCredentials()
    val region = getRegion()
    println(s"AWS Access Key: ${credentials.accessKeyId()}")
    println(s"Region currently being used: $region")

    val kinesisClient = KinesisClient
      .builder()
      .httpClient(ApacheHttpClient.create())
      .region(region)
      .build()
    val numShards = getNumShards(kinesisClient, streamName)
    println(s"Number of shards: $numShards")
    // Each Kinesis shard becomes its own input DStream
    val kinesisStreams = (0 until numShards).map { i =>
      KinesisInputDStream.builder
        .streamingContext(ssc)
        .endpointUrl(endpointUrl)
        .regionName(region.toString())
        .streamName(streamName)
        .initialPosition(new KinesisInitialPositions.Latest())
        .checkpointAppName(appName)
        .checkpointInterval(Milliseconds(100))
        .storageLevel(StorageLevel.MEMORY_AND_DISK_2)
        .build()
    }
    // Unifying input DStreams
    val unionStreams = ssc.union(kinesisStreams)
    val inputStream = getInputStream(unionStreams)

    writeToDynamodb(inputStream, region.toString(), tableName)

    ssc.start()
    ssc.awaitTermination()
  }
}
