package com.amazonreviews.streaming

import com.amazonreviews.streaming.KinesisSparkHelper.{
  processAPIData,
  processReviewJson
}
import com.amazonreviews.streaming.ReviewFixture.reviewJson
import org.scalatest.FunSuite
import breeze.numerics.exp

class KinesisSparkHelperTests extends FunSuite {
  test("processAPIData is invoked on a json String") {
    val f = reviewJson
    val actual = processAPIData(f.jsonString)
    val expected = ProductReviewDynamo(
      f.id,
      f.reviewId,
      f.expectedJson
    )
    assert(actual == expected)
  }

  test("processReviewJson is invoked on the review json String") {
    val f = reviewJson
    val actual = processReviewJson(f.jsonString)
    val expected = ProductReviewRedshift(
      id = f.id,
      reviewId = f.reviewId,
      name = f.name,
      brand = f.brand,
      reviewDate = f.reviewDate,
      reviewDoRecommend = f.reviewDoRecommend,
      reviewNumHelpful = f.reviewNumHelpful.toInt,
      reviewRating = f.reviewRating.toInt,
      reviewText = f.reviewText,
      reviewTitle = f.reviewTitle,
      reviewUsername = f.reviewUsername
    )
    assert(actual == expected)
  }
}
