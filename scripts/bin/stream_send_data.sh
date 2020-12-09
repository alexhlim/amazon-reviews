#!/bin/bash
source $AMAZON_REVIEWS_CONFIG

python ${REPO_LOCATION}/scripts/python/client.py $API_GATEWAY_ENDPOINT $REVIEW_CSV