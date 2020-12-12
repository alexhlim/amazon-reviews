#!/bin/bash
source $AMAZON_REVIEWS_CONFIG

# Running stream app using spark submit
sbt stream/clean/assembly && \
"${SPARK_PATH}/bin/spark-submit" \
--jars $REDSHIFT_DRIVER_JAR \
${REPO_LOCATION}/${STREAM_JAR_PATH} \
$STREAM_APP_NAME \
$KINESIS_STREAM $KINESIS_ENDPOINT \
$DYNAMODB_TABLE \
$REDSHIFT_JDBC $REDSHIFT_JDBC_CLASS $REDSHIFT_TABLE