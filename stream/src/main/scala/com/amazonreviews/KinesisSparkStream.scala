package com.amazonreviews.stream

import com.amazonreviews.util.KinesisSparkHelper.{
  getNumShards,
  getCredentials,
  getRegion,
  processAPIDataDynamo,
  processAPIDataRedshift,
  ProductReviewDynamo,
  ProductReviewRedshift
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

object KinesisSparkStream {

  def convertStreamDynamo(
      unionStreams: DStream[Array[Byte]]
  ): DStream[ProductReviewDynamo] = {
    val dynamoStream = unionStreams.map { byteArray =>
      val jsonString = new String(byteArray)
      processAPIDataDynamo(jsonString)
    }
    dynamoStream
  }

  def convertStreamRedshift(
      unionStreams: DStream[Array[Byte]]
  ): DStream[ProductReviewRedshift] = {
    val redshiftStream = unionStreams.map { byteArray =>
      val jsonString = new String(byteArray)
      processAPIDataRedshift(jsonString)
    }
    redshiftStream
  }

  def writeToDynamodb(
      dynamoStream: DStream[ProductReviewDynamo],
      region: String,
      dynamoTable: String
  ): Unit = {
    // Process only current batch
    dynamoStream.foreachRDD { rdd =>
      val spark = SparkSession.builder().getOrCreate()
      // Spark needs to be initalized before importing this, needed for .toDF()
      import spark.implicits._
      val reviewDf = rdd.toDF()
      if (!reviewDf.isEmpty) {
        val dynamoDf =
          reviewDf.groupBy("id").pivot("reviewId").agg(first("reviewJson"))
        dynamoDf.show(1)
        dynamoDf.write.option("region", region.toString()).dynamodb(dynamoTable)
      }
    }
  }

  def writeToRedshift(
      redshiftStream: DStream[ProductReviewRedshift],
      redshiftJdbc: String,
      redshiftJdbcClass: String,
      redshiftUsername: String,
      redshiftPassword: String,
      redshiftTable: String
  ): Unit = {
    redshiftStream.foreachRDD { rdd =>
      val spark = SparkSession.builder().getOrCreate()
      // Spark needs to be initalized before importing this, needed for .toDF()
      import spark.implicits._
      val reviewDf = rdd.toDF()
      if (!reviewDf.isEmpty) {
        reviewDf.show(1)
        reviewDf.write
          .format("jdbc")
          .option("url", redshiftJdbc)
          .option("driver", redshiftJdbcClass)
          .option("dbtable", redshiftTable)
          .option("user", redshiftUsername)
          .option("password", redshiftPassword)
          .mode("append")
          .save()
      }
    }
  }

  def main(args: Array[String]) {
    val Array(
      appName,
      kinesisStream,
      kinesisEndpoint,
      dynamoTable,
      redshiftJdbc,
      redshiftJdbcClass,
      redshiftUsername,
      redshiftPassword,
      redshiftTable
    ) = args

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
    val numShards = getNumShards(kinesisClient, kinesisStream)
    println(s"Number of shards: $numShards")
    // Each Kinesis shard becomes its own input DStream
    val kinesisStreams = (0 until numShards).map { i =>
      KinesisInputDStream.builder
        .streamingContext(ssc)
        .endpointUrl(kinesisEndpoint)
        .regionName(region.toString())
        .streamName(kinesisStream)
        .initialPosition(new KinesisInitialPositions.Latest())
        .checkpointAppName(appName)
        .checkpointInterval(Milliseconds(100))
        .storageLevel(StorageLevel.MEMORY_AND_DISK_2)
        .build()
    }
    // Unifying input DStreams
    val unionStreams = ssc.union(kinesisStreams)
    val dynamoStream = convertStreamDynamo(unionStreams)
    val redshiftStream = convertStreamRedshift(unionStreams)

    writeToDynamodb(dynamoStream, region.toString(), dynamoTable)
    writeToRedshift(
      redshiftStream,
      redshiftJdbc,
      redshiftJdbcClass,
      redshiftUsername,
      redshiftPassword,
      redshiftTable
    )

    ssc.start()
    ssc.awaitTermination()
  }
}
