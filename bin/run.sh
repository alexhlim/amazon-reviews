#!/bin/bash
source .amazon-reviews-config

SPARK_PATH=~/Documents/dev/spark-3.0.1-bin-hadoop3.2
AMAZON_REVIEWS_PATH=~/Documents/dev/amazon-reviews

sbt clean assembly && \
"${SPARK_PATH}/bin/spark-submit" \
${AMAZON_REVIEWS_PATH}/${AMAZON_REVIEWS_JAR_PATH} \
$APP_NAME $KINESIS_STREAM $KINESIS_ENDPOINT $DYNAMODB_TABLE $REDSHIFT_USERNAME $REDSHIFT_PASSWORD $REDSHIFT_TABLE $REDSHIFT_S3_PATH