#!/bin/bash
export APP_NAME=AmazonReviews
export STREAM_NAME=AmazonReviews-APIData
export ENDPOINT=https://kinesis.us-east-2.amazonaws.com
export DYNAMODB_TABLE=AmazonReviews-Product
export SPARK_PATH=~/Documents/dev/spark-3.0.1-bin-hadoop3.2
export AMAZON_REVIEWS_PATH=~/Documents/dev/amazon-reviews

sbt clean assembly && \
"${SPARK_PATH}/bin/spark-submit" \
"${AMAZON_REVIEWS_PATH}/target/scala-2.12/amazon-reviews-streaming-assembly-0.1.jar" \
${APP_NAME} ${STREAM_NAME} ${ENDPOINT} ${DYNAMODB_TABLE}