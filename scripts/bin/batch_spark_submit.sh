#!/bin/bash
source $AMAZON_REVIEWS_CONFIG

"${SPARK_PATH}/bin/spark-submit" \
--name $BATCH_APP_NAME \
--master local[2] \
--jars $REDSHIFT_DRIVER_JAR \
--packages com.amazonaws:aws-java-sdk:1.7.4,org.apache.hadoop:hadoop-aws:2.7.7 \
--conf spark.hadoop.fs.s3a.impl=org.apache.hadoop.fs.s3a.S3AFileSystem \
--conf spark.hadoop.fs.s3a.endpoint=$BATCH_S3_ENDPOINT \
--conf spark.executor.extraJavaOptions=-Dcom.amazonaws.services.s3.enableV4=true \
--conf spark.driver.extraJavaOptions=-Dcom.amazonaws.services.s3.enableV4=true \
${REPO_LOCATION}/${BATCH_JAR} \
$STREAM_APP_NAME $BATCH_S3_DIR $REDSHIFT_JDBC $REDSHIFT_JDBC_CLASS $REDSHIFT_TABLE

