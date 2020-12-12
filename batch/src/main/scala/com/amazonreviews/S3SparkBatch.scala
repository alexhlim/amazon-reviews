package com.amazonreviews.batch

import com.amazonreviews.util.Helper.dfToRedshift
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col

object S3SparkBatch {
  /** Functions for running batch app. **/

  /** Run batch app utlizing S3 and Spark. Checks to see if data is present in
   *  specified S3 bucket. Transforms the structure of the data using ProductReviewRedshift and writes 
   *  to its corresponding table. For usage, see scripts/bin/batch_spark_submit.sh
   *  (make sure amazon-reviews-config is filled and exported before running).
   *  
   *  Example:
   * $SPARK_PATH/bin/spark-submit" \
   * --name AmazonReviews-Batch\
   * --master local[2] \
   * --jars https://s3.amazonaws.com/redshift-downloads/drivers/jdbc/1.2.45.1069/RedshiftJDBC42-no-awssdk-1.2.45.1069.jar \
   * --packages com.amazonaws:aws-java-sdk:1.7.4,org.apache.hadoop:hadoop-aws:2.7.7 \
   * --conf spark.hadoop.fs.s3a.impl=org.apache.hadoop.fs.s3a.S3AFileSystem \
   * --conf spark.hadoop.fs.s3a.endpoint=s3.us-east-2.amazonaws.com\
   * --conf spark.executor.extraJavaOptions=-Dcom.amazonaws.services.s3.enableV4=true \
   * --conf spark.driver.extraJavaOptions=-Dcom.amazonaws.services.s3.enableV4=true \
   * batch/target/scala-2.12/batch-assembly-1.0.0.jar \
   * <batch-app-name> <batch-s3-dir> <redshift-jdbc> <redshift-jdbc-class> <redshift-table>
   * 
   * @param appName batch app name
   * @param s3Directory s3 bucket
   * @param redshiftJdbc redshift jdbc url
   * @param redhisftJdbcClass class of jdbc driver
   * @param redshiftTable redshift table name
   */
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
