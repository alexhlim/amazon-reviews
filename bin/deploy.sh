#!/bin/bash
source .amazon-reviews-config

sbt clean assembly && \
aws s3 cp $AMAZON_REVIEWS_JAR_PATH/$AMAZON_REVIEWS_JAR $AMAZON_REVIEWS_S3 && \
aws emr add-steps \
--cluster-id $CLUSTER_ID \
--steps \
Type=Spark,\
Name=$APP_NAME,\
ActionOnFailure=CONTINUE,\
Args=["$AMAZON_REVIEWS_S3/$AMAZON_REVIEWS_JAR",$APP_NAME,$STREAM_NAME,$KINESIS_ENDPOINT,$DYNAMODB_TABLE]