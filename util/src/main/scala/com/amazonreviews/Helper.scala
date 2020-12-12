package com.amazonreviews.util

import software.amazon.awssdk.auth.credentials.{
  DefaultCredentialsProvider,
  AwsCredentials
}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.kinesis.KinesisClient
import software.amazon.awssdk.services.kinesis.model.{DescribeStreamRequest}
import org.apache.spark.sql.DataFrame
import org.json4s._
import org.json4s.jackson.JsonMethods._

object Helper {
  /** Helper functions for stream and batch projects. **/

  /** Schema for dynamodb table (wide column store) s**/
  case class ProductReviewDynamo(
      id: String,
      reviewId: String,
      reviewJson: String
  )

  /** Schema for redshift table (relational) **/
  case class ProductReviewRedshift(
      id: String,
      reviewId: String,
      name: String,
      brand: String,
      reviewDate: String,
      reviewDoRecommend: String,
      reviewNumHelpful: Int,
      reviewRating: Int,
      reviewText: String,
      reviewTitle: String,
      reviewUsername: String
  )

  /** Fetch number of shards in a given kinesis stream **/
  def getNumShards(kinesisClient: KinesisClient, streamName: String): Int = {
    val describeStreamRequest =
      DescribeStreamRequest.builder().streamName(streamName).build()
    kinesisClient
      .describeStream(describeStreamRequest)
      .streamDescription()
      .shards()
      .size
  }

  /** Retrieve AWS credentials **/
  def getCredentials(): AwsCredentials = {
    val credentials = DefaultCredentialsProvider.create().resolveCredentials()
    require(
      credentials != null,
      """""
      No AWS credentials found. Please provide using one of methods described in
      http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html
      """"
    )
    credentials
  }

  /** AWS region **/
  def getRegion(): Region = {
    // See if you can pull this dynamically
    Region.US_EAST_2
  }

  /** Transforming JSON to ProductReviewDynamo schema **/
  def processAPIDataDynamo(jsonString: String): ProductReviewDynamo = {
    val json = parse(jsonString)
    val id = compact(render(json \ "id"))
    val reviewId = compact(render(json \ "reviews.id"))
    // Removing ids from JSON
    val reviewJson = compact(render(json removeField {
      case JField("id", _)         => true
      case JField("reviews.id", _) => true
      case _                       => false
    }))
    ProductReviewDynamo(id, reviewId, reviewJson)
  }

  /** Transforming JSON to ProductReviewRedshift schema **/
  def processAPIDataRedshift(reviewJson: String): ProductReviewRedshift = {
    val json = parse(reviewJson)
    implicit val formats = DefaultFormats
    val schema = ProductReviewRedshift(
      id = compact(render(json \ "id")),
      reviewId = compact(render(json \ "reviews.id")),
      name = compact(render(json \ "name")),
      brand = compact(render(json \ "brand")),
      reviewDate = compact(render(json \ "reviews.date")),
      reviewDoRecommend = compact(render(json \ "reviews.doRecommend")),
      reviewNumHelpful = (json \ "reviews.numHelpful").extract[Int],
      reviewRating = (json \ "reviews.rating").extract[Int],
      reviewText = compact(render(json \ "reviews.text")),
      reviewTitle = compact(render(json \ "reviews.title")),
      reviewUsername = compact(render(json \ "reviews.username"))
    )
    schema
  }

  /** Write Spark DF to redshift **/
  def dfToRedshift(
      df: DataFrame,
      redshiftJdbc: String,
      redshiftJdbcClass: String,
      redshiftUsername: String,
      redshiftPassword: String,
      redshiftTable: String
  ): Unit = {
    if (!df.isEmpty) {
      df.show(1)
      df.write
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
