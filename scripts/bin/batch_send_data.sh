#!/bin/bash
source .amazon-reviews-config

if [[ ${BATCH_DATA_TO_SEND: -4} == ".csv" ]]
then
    echo Sending to $BATCH_S3_DIR...
    aws s3 cp $BATCH_DATA_TO_SEND $BATCH_S3_DIR
else
    echo BATCH_DATA_TO_SEND must be a csv
    exit
fi
