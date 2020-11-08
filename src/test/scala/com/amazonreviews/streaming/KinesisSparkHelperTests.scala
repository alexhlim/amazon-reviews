package com.amazonreviews.streaming

import com.amazonreviews.streaming.KinesisSparkHelper.processAPIData
import com.amazonreviews.streaming.ReviewFixture.reviewJson
import org.scalatest.FunSuite

class KinesisSparkHelperTests extends FunSuite {
  test("processAPIData is invoked on a json String") {
    val f = reviewJson
    val actual = processAPIData(f.jsonString)
    val expected = ProductReviewData(
      f.id,
      f.reviewId,
      f.expectedJson
    )
    assert(actual == expected)
  }
}
