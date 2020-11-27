package com.amazonreviews.util

import com.amazonreviews.util.KinesisSparkHelper.{
  processAPIDataDynamo,
  processAPIDataRedshift
}
import com.amazonreviews.util.ReviewFixture.reviewJson

import org.scalatest.FunSuite

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
