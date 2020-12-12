#!/bin/bash
source $AMAZON_REVIEWS_CONFIG

# Wrapper to send data to AWS API Gateway
python ${REPO_LOCATION}/scripts/python/client.py $API_GATEWAY_ENDPOINT $REVIEW_CSV