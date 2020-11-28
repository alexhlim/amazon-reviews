#!/bin/bash
source .amazon-reviews-config

mkdir -p data

kaggle datasets download \
datafiniti/consumer-reviews-of-amazon-products \
-f 1429_1.csv \
-p data/

python ${REPO_LOCATION}/scripts/python/generate_data.py $REPO_LOCATION $SAMPLE_DATA_SIZE