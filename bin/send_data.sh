#!/bin/bash
source .amazon-reviews-config

python client.py $API_GATEWAY_ENDPOINT $REVIEW_CSV