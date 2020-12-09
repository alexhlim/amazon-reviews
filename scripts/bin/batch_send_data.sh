#!/bin/bash
source $AMAZON_REVIEWS_CONFIG

if [[ ${BATCH_DATA_TO_SEND: -4} == ".csv" ]]
then
    echo Sending to $BATCH_S3_DIR...
    aws s3 cp $REVIEW_CSV $BATCH_S3_DIR
else
    echo BATCH_DATA_TO_SEND must be a csv
    exit
fi
