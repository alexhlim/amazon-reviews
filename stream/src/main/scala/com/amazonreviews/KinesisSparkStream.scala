package com.amazonreviews.stream

import com.amazonreviews.util.Helper.{
  getNumShards,
  getCredentials,
  getRegion,
  processAPIDataDynamo,
  processAPIDataRedshift,
  dfToRedshift,
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
  /** Functions for running streaming app. **/

  /** Converts kinesis stream to dynamo schema **/
  def convertStreamDynamo(
      unionStreams: DStream[Array[Byte]]
  ): DStream[ProductReviewDynamo] = {
    val dynamoStream = unionStreams.map { byteArray =>
      val jsonString = new String(byteArray)
      processAPIDataDynamo(jsonString)
    }
    dynamoStream
  }

  /** Converts kinesis stream to redshift schema **/
  def convertStreamRedshift(
      unionStreams: DStream[Array[Byte]]
  ): DStream[ProductReviewRedshift] = {
    val redshiftStream = unionStreams.map { byteArray =>
      val jsonString = new String(byteArray)
      processAPIDataRedshift(jsonString)
    }
    redshiftStream
  }

  /** Write stream to dynamo **/
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

  /** Write stream to redshift **/
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
      dfToRedshift(
        reviewDf,
        redshiftJdbc,
        redshiftJdbcClass,
        redshiftUsername,
        redshiftPassword,
        redshiftTable
      )
    }
  }

  /** Run stream app utlizing Kinesis and Spark. Checks to see if data is present in
   *  Kinesis using Spark Streaming + Kinesis integration, where the number of streams
   *  is equivalent to the number of shards. Transforms the structure of the data using
   *  ProductReviewDynamo and ProductReviewRedshift and writes to its corresponding tables.
   *  For usage, see scripts/bin/stream_run.sh (make sure amazon-reviews-config is filled and exported before running).
   *  
   *  Example:
   *    "$SPARK_PATH/bin/spark-submit" \
   *    --jars https://s3.amazonaws.com/redshift-downloads/drivers/jdbc/1.2.45.1069/RedshiftJDBC42-no-awssdk-1.2.45.1069.jar \
   *    stream/target/scala-2.12/stream-assembly-1.0.0.jar \
   *    <app-name> \
   *    <kinesis-stream> <kinesis-endpoint> \
   *    <dynamodb-table> \
   *    <redshift-jdbc> <redshift-jdbc-class> <redshift-table>
   * 
   * @param appName stream app name
   * @param kinesisStream name of Kinesis stream
   * @param kinesisEndpoint kinesis endpoint
   * @param dynamoTable dynamodb table name
   * @param redshiftJdbc redshift jdbc url
   * @param redhisftJdbcClass class of jdbc driver
   * @param redshiftTable redshift table name
   */
  def main(args: Array[String]) {
    val Array(
      appName,
      kinesisStream,
      kinesisEndpoint,
      dynamoTable,
      redshiftJdbc,
      redshiftJdbcClass,
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
      sc.getConf.get("spark.redshift.username"),
      sc.getConf.get("spark.redshift.password"),
      redshiftTable
    )

    ssc.start()
    ssc.awaitTermination()
  }
}
