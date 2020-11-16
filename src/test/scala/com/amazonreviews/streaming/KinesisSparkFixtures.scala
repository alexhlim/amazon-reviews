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
    val id = """"AVqkIhwDv8e3D1O-lebb""""
    val reviewId = """"1""""
    val name =
      """"All-New Fire HD 8 Tablet, 8 HD Display, Wi-Fi, 16 GB - Includes Special Offers, Magenta""""
    val brand = """"Amazon""""
    val reviewDate = """"2017-01-13T00:00:00.000Z""""
    val reviewDoRecommend = """"true""""
    val reviewNumHelpful = """0"""
    val reviewRating = """5"""
    val reviewText =
      """"This product so far has not disappointed. My children love to use it and I like the ability to monitor control what content they see with ease.""""
    val reviewTitle = """"Kindle""""
    val reviewUsername = """"Adapter""""
    val jsonString =
      s"""{"id":${id},"name":${name},"brand":${brand},"reviews.date":${reviewDate},"reviews.doRecommend":${reviewDoRecommend},"reviews.id":${reviewId},"reviews.numHelpful":${reviewNumHelpful},"reviews.rating":${reviewRating},"reviews.text":${reviewText},"reviews.title":${reviewTitle},"reviews.username":${reviewUsername}}"""
    val expectedJson =
      s"""{"name":${name},"brand":${brand},"reviews.date":${reviewDate},"reviews.doRecommend":${reviewDoRecommend},"reviews.numHelpful":${reviewNumHelpful},"reviews.rating":${reviewRating},"reviews.text":${reviewText},"reviews.title":${reviewTitle},"reviews.username":${reviewUsername}}"""
  }

}
