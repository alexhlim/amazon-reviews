package com.amazonreviews.streaming

import com.amazonreviews.streaming.KinesisSparkStreamDynamo.getInputStream
import com.amazonreviews.streaming.ReviewFixture.reviewJson
import com.holdenkarau.spark.testing.{StreamingSuiteBase, DataFrameSuiteBase}
import org.scalatest.FunSuite
import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.functions._

class KinesisSparkStreamDynamoTests
    extends FunSuite
    with StreamingSuiteBase
    with DataFrameSuiteBase {
  test("getInputStream creates DStream of ProductReviewData") {
    val f = reviewJson
    val input = List(List(f.jsonString.getBytes()))
    val expected = List(
      List(
        ProductReviewData(
          f.id,
          f.reviewId,
          f.expectedJson
        )
      )
    )
    testOperation[Array[Byte], ProductReviewData](
      input,
      getInputStream _,
      expected,
      ordered = false
    )
  }

  test("pivoting ProductReviewData to dynamodb wide column format") {
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
