package com.amazonreviews.batch

import com.amazonreviews.util.Helper.dfToRedshift
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col

object S3SparkBatch {

  def main(args: Array[String]) = {
    val Array(
      appName,
      s3Directory,
      redshiftJdbc,
      redshiftJdbcClass,
      redshiftTable
    ) = args

    val sc = new SparkContext(new SparkConf())
    sc.setLogLevel("ERROR")
    println(s"Launching: ${appName}")

    val spark = SparkSession
      .builder()
      .appName(appName)
      .getOrCreate()

    import spark.implicits._
    val s3aDirectory = s3Directory.replace("s3://", "s3a://")
    val df =
      spark.read
        .format("csv")
        .option("header", "true")
        .load(s3aDirectory)
    val lookup =
      Map(
        "id" -> "id",
        "reviews_id" -> "reviewId",
        "name" -> "name",
        "brand" -> "brand",
        "reviews_date" -> "reviewDate",
        "reviews_doRecommend" -> "reviewDoRecommend",
        "reviews_numHelpful" -> "reviewNumHelpful",
        "reviews_rating" -> "reviewRating",
        "reviews_text" -> "reviewText",
        "reviews_title" -> "reviewTitle",
        "reviews_username" -> "reviewUsername"
      )
    // Replace dots to underscores to avoid backticks in column names (ie. reviews.id)
    val underscoreDf = df.toDF(df.columns.map(_.replace(".", "_")): _*)
    // Selecting on ProductReviewRedshift columns
    @SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
    val filteredDf =
      underscoreDf.select(
        lookup.keys.toList.head,
        lookup.keys.toList.tail: _*
      )
    // Renaming columns
    val redshiftDf = filteredDf.select(
      filteredDf.columns.map(c => col(c).as(lookup.getOrElse(c, c))): _*
    )
    dfToRedshift(
      redshiftDf,
      redshiftJdbc,
      redshiftJdbcClass,
      spark.conf.get("spark.redshift.username"),
      spark.conf.get("spark.redshift.password"),
      redshiftTable
    )
  }
}
