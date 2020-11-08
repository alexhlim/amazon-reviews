package com.amazonreviews.streaming

import com.amazonreviews.streaming.KinesisSparkStreamDynamo.getInputStream
import com.amazonreviews.streaming.ReviewFixture.reviewJson
import com.holdenkarau.spark.testing.StreamingSuiteBase
import org.scalatest.FunSuite

class KinesisSparkStreamDynamoTests extends FunSuite with StreamingSuiteBase {
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

}
