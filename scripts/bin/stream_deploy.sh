#!/bin/bash
source $AMAZON_REVIEWS_CONFIG

# Deploy stream app to AWS EMR cluster -- make sure you fill out cluster id in AMAZON_REVIEWS_CONFIG
sbt stream/clean/assembly && \
aws s3 cp $STREAM_JAR_PATH $EMR_S3_PATH && \
aws emr add-steps \
--cluster-id $EMR_CLUSTER_ID \
--steps \
Type=Spark,\
Name=$APP_NAME,\
ActionOnFailure=CONTINUE,\
Args=[--jars,$REDSHIFT_DRIVER_JAR,$S3_BUCKET/$STREAM_JAR,$STREAM_APP_NAME,$KINESIS_STREAM,$KINESIS_ENDPOINT,$DYNAMODB_TABLE,$REDSHIFT_JDBC,$REDSHIFT_JDBC_CLASS,$REDSHIFT_USERNAME,$REDSHIFT_PASSWORD,$REDSHIFT_TABLE]