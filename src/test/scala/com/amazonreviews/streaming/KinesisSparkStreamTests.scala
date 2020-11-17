package com.amazonreviews.streaming

import com.amazonreviews.streaming.KinesisSparkStream.{
  convertStreamDynamo,
  convertStreamRedshift
}
import com.amazonreviews.streaming.ReviewFixture.reviewJson
import com.holdenkarau.spark.testing.{StreamingSuiteBase, DataFrameSuiteBase}
import org.scalatest.FunSuite
import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.functions._

class KinesisSparkStreamDynamoTests
    extends FunSuite
    with StreamingSuiteBase
    with DataFrameSuiteBase {
  test("convertStreamDynamo creates DStream of ProductReviewDynamo") {
    val f = reviewJson
    val input = List(List(f.jsonString.getBytes()))
    val expected = List(
      List(
        f.productReviewDynamo
      )
    )
    testOperation[Array[Byte], ProductReviewDynamo](
      input,
      convertStreamDynamo _,
      expected,
      ordered = false
    )
  }

  test("convertStreamRedshift creates DStream of ProductReviewRedshift") {
    val f = reviewJson
    val input = List(List(f.jsonString.getBytes()))
    val expected = List(
      List(
        f.productReviewRedshift
      )
    )
    testOperation[Array[Byte], ProductReviewRedshift](
      input,
      convertStreamRedshift _,
      expected,
      ordered = false
    )
  }

  test("pivoting ProductReviewDynamo to dynamodb wide column format") {
    val f = reviewJson
    val sqlCtx = sqlContext
    import sqlCtx.implicits._

    val input = sc
      .parallelize(
        List[(String, String, String)](
          ("prod1", "prod1review1", "json1"),
          ("prod1", "prod1review2", "json2"),
          ("prod2", "prod2review1", "json3"),
          ("prod2", "prod2review2", "json4")
        )
      )
      .toDF("id", "reviewId", "reviewJson")
    val actual = input.groupBy("id").pivot("reviewId").agg(first("reviewJson"))
    @SuppressWarnings(Array("org.wartremover.warts.Null"))
    val expected = sc
      .parallelize(
        List[(String, String, String, String, String)](
          ("prod1", "json1", "json2", null, null),
          ("prod2", null, null, "json3", "json4")
        )
      )
      .toDF(
        "id",
        "prod1review1",
        "prod1review2",
        "prod2review1",
        "prod2review2"
      )
    assertDataFrameEquals(actual, expected)
  }

}
