package com.amazonreviews.streaming

import software.amazon.awssdk.auth.credentials.{
  DefaultCredentialsProvider,
  AwsCredentials
}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.kinesis.KinesisClient
import software.amazon.awssdk.services.kinesis.model.{DescribeStreamRequest}
import org.json4s._
import org.json4s.jackson.JsonMethods._

object KinesisSparkHelper {

  def getNumShards(kinesisClient: KinesisClient, streamName: String): Int = {
    val describeStreamRequest =
      DescribeStreamRequest.builder().streamName(streamName).build()
    kinesisClient
      .describeStream(describeStreamRequest)
      .streamDescription()
      .shards()
      .size
  }

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

  def getRegion(): Region = {
    // See if you can pull this dynamically
    Region.US_EAST_2
  }

  def processAPIData(jsonString: String): ProductReviewData = {
    // Move to helpers
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

}
case class ProductReviewData(id: String, reviewId: String, reviewJson: String)
