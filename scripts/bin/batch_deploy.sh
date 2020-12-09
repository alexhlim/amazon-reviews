#!/bin/bash
source $AMAZON_REVIEWS_CONFIG

# Assembling jar and sending to S3
sbt batch/clean/assembly && \
aws s3 cp $BATCH_JAR_PATH $S3_BUCKET