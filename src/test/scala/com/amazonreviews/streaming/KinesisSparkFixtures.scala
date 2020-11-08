package com.amazonreviews.streaming

class ReviewFixture(
    id: String,
    reviewId: String,
    jsonString: String,
    expectedJson: String
)

object ReviewFixture {
  def reviewJson = new {
    // Four double quotes to get String representation: "123", "xyz"
    val id = """"123""""
    val reviewId = """"xyz""""
    val jsonString =
      s"""{"id":${id},"reviews.id":${reviewId},"other":"blah"}"""
    val expectedJson = """{"other":"blah"}"""
  }
}
