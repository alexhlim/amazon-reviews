#!/bin/bash
source $AMAZON_REVIEWS_CONFIG

# Wrapper to send data to AWS API Gateway
# Usage for python:
# python $REPO_LOCATION/scripts/python/client.py <api-gateway-endpoint> <review-csv>
python $REPO_LOCATION/scripts/python/client.py $API_GATEWAY_ENDPOINT $REVIEW_CSV