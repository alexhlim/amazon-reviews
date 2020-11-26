// package com.amazonreviews.batch

// import org.apache.spark.{SparkContext, SparkConf}
// import org.apache.spark.sql.SparkSession

// object KinesisSparkBatch {

//   def main(args: Array[String]) = {
//     val Array(
//       appName
//       //   kinesisStream,
//       //   kinesisEndpoint,
//       //   dynamoTable,
//       //   redshiftJdbc,
//       //   redshiftJdbcClass,
//       //   redshiftUsername,
//       //   redshiftPassword,
//       //   redshiftTable
//     ) = args

//     val conf = new SparkConf().setMaster("local[2]").setAppName(appName)
//     val sc = new SparkContext(conf)
//     // conf.sc.setLogLevel("ERROR")
//     println(s"Launching: ${appName}")

//     val filePath = "s3a://amazonreviews-analytics/batch/sample.csv"

//     val spark = SparkSession
//       .builder()
//       .appName(appName)
//       .getOrCreate()

//     import spark.implicits._
//     val df = spark.read.format("csv").option("header", "true").load(filePath)
//   }
// }
