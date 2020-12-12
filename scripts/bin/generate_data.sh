#!/bin/bash
# Downloads Amazon Review data from Kaggle and creates a sample dataset.
source $AMAZON_REVIEWS_CONFIG

# Create data directory and use 
mkdir -p data

# Use Kaggle API to download Amazon Reviews dataset
kaggle datasets download \
datafiniti/consumer-reviews-of-amazon-products \
-f 1429_1.csv \
-p data/

# Generate sample data (specify in config)
python ${REPO_LOCATION}/scripts/python/generate_data.py $REPO_LOCATION $SAMPLE_DATA_SIZE