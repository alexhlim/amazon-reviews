package com.amazonreviews.streaming

import com.amazonreviews.streaming.KinesisSparkHelper.{
  processAPIDataDynamo,
  processAPIDataRedshift
}
import com.amazonreviews.streaming.ReviewFixture.reviewJson
import org.scalatest.FunSuite
import breeze.numerics.exp

class KinesisSparkHelperTests extends FunSuite {
  test("processAPIDataDynamo is invoked on a json String") {
    val f = reviewJson
    val actual = processAPIDataDynamo(f.jsonString)
    val expected = f.productReviewDynamo
    assert(actual == expected)
  }

  test("processAPIDataRedshift is invoked on a json String") {
    val f = reviewJson
    val actual = processAPIDataRedshift(f.jsonString)
    val expected = f.productReviewRedshift
    assert(actual == expected)
  }
}
