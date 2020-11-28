#!/bin/bash
source .amazon-reviews-config

python ${REPO_LOCATION}/scripts/python/client.py $API_GATEWAY_ENDPOINT $REVIEW_CSV