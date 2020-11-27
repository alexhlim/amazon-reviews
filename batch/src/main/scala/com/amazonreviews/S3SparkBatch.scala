package com.amazonreviews.batch

import com.amazonreviews.util.Helper.dfToRedshift
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.sql.SparkSession

object S3SparkBatch {

  def main(args: Array[String]) = {
    val Array(
      appName,
      s3Directory,
      redshiftJdbc,
      redshiftJdbcClass,
      redshiftUsername,
      redshiftPassword,
      redshiftTable
    ) = args

    val conf = new SparkConf().setMaster("local[2]").setAppName(appName)
    val sc = new SparkContext(conf)
    sc.setLogLevel("ERROR")
    println(s"Launching: ${appName}")

    val spark = SparkSession
      .builder()
      .appName(appName)
      .getOrCreate()

    import spark.implicits._
    val df = spark.read.format("csv").option("header", "true").load(s3Directory)
    dfToRedshift(
      df,
      redshiftJdbc,
      redshiftJdbcClass,
      redshiftUsername,
      redshiftPassword,
      redshiftTable
    )
  }
}
