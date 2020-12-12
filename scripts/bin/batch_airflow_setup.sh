#!/bin/bash
# Contains functions to be easily used on the CLI to start airflow.
# Make sure $AMAZON_REVIEWS_CONFIG is set before sourcing
source $AMAZON_REVIEWS_CONFIG

# Helper function when launching new shell tabs
# Sets airflow and config variables + activates environment
setup_env() {
    export AIRFLOW_HOME=$REPO_LOCATION && \
    export AMAZON_REVIEWS_CONFIG=$REPO_LOCATION/.amazon-reviews-config && \
    conda activate $CONDA_ENV
}

# For the following functions, install ttab: npm install -g ttab
# Start airflow local webserver
start_webserver() {
    ttab "export AMAZON_REVIEWS_CONFIG=$AMAZON_REVIEWS_CONFIG && \
    source $REPO_LOCATION/scripts/bin/batch_airflow_setup.sh && \
    setup_env && \
    airflow webserver --port=80 --pid=$AIRFLOW_HOME/airflow-webserver.pid"
}

# Start airflow local scheduler
start_scheduler() {
   ttab "export AMAZON_REVIEWS_CONFIG=$AMAZON_REVIEWS_CONFIG && \
   source $REPO_LOCATION/scripts/bin/batch_airflow_setup.sh && \
   setup_env && \
   airflow scheduler"
}
