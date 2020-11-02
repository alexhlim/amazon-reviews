package com.amazonreviews.streaming

import com.amazonreviews.streaming.KinesisSparkHelper.{processAPIData}
import org.scalatest.funsuite.{AnyFunSuite}

class KinesisSparkHelperTests extends AnyFunSuite {
  test("processAPIData is invoked on a json String") {
    // Four double quotes to get String representation: "123", "xyz"
    val id = s""""123""""
    val reviewId = s""""xyz""""
    val jsonString =
      s"""{"id":${id},"reviews.id":${reviewId},"other":"blah"}"""
    val actual = processAPIData(jsonString)
    val expected = ProductReviewData(
      id,
      reviewId,
      """{"other":"blah"}"""
    )
    assert(actual == expected)
  }
}
