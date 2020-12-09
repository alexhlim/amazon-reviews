#!/bin/bash
source $AMAZON_REVIEWS_CONFIG

setup_env() {
    export AIRFLOW_HOME=${REPO_LOCATION} && \
    export AMAZON_REVIEWS_CONFIG=${REPO_LOCATION}/.amazon-reviews-config && \
    conda activate ${CONDA_ENV}
}

# npm install -g ttab
start_webserver() {
    ttab "export AMAZON_REVIEWS_CONFIG=$AMAZON_REVIEWS_CONFIG && \
    source ${REPO_LOCATION}/scripts/bin/batch_airflow_setup.sh && \
    setup_env && \
    airflow webserver --port=80 --pid=$AIRFLOW_HOME/airflow-webserver.pid"
}

start_scheduler() {
   ttab "export AMAZON_REVIEWS_CONFIG=$AMAZON_REVIEWS_CONFIG && \
   source ${REPO_LOCATION}/scripts/bin/batch_airflow_setup.sh && \
   setup_env && \
   airflow scheduler"
}
