#!/bin/bash
source .amazon-reviews-config

sbt clean assembly && \
aws s3 cp $STREAM_JAR_PATH $EMR_S3_PATH && \
aws emr add-steps \
--cluster-id $EMR_CLUSTER_ID \
--steps \
Type=Spark,\
Name=$APP_NAME,\
ActionOnFailure=CONTINUE,\
Args=[--jars,$REDSHIFT_DRIVER_JAR,$EMR_S3_PATH/$AMAZON_REVIEWS_JAR,$APP_NAME,$KINESIS_STREAM,$KINESIS_ENDPOINT,$DYNAMODB_TABLE,$REDSHIFT_JDBC,$REDSHIFT_JDBC_CLASS,$REDSHIFT_USERNAME,$REDSHIFT_PASSWORD,$REDSHIFT_TABLE]