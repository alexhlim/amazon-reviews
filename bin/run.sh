#!/bin/bash
export APP_NAME=AmazonReviews
export STREAM_NAME=AmazonReviews-APIData
export ENDPOINT=https://kinesis.us-east-2.amazonaws.com
export DYNAMODB_TABLE=AmazonReviews-Product

exec sbt "run ${APP_NAME} ${STREAM_NAME} ${ENDPOINT} ${DYNAMODB_TABLE}"