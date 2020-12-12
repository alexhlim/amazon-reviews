"""Dag for batch processing."""
import os
from configparser import ConfigParser
from datetime import timedelta

from airflow import DAG
from airflow.operators.bash_operator import BashOperator
from airflow.operators.dummy_operator import DummyOperator
from airflow.utils.dates import days_ago

try:
    config_path = os.environ["AMAZON_REVIEWS_CONFIG"]
    config = ConfigParser()
    config.read(config_path)
except KeyError:
    exit("Please set AMAZON_REVIEWS_CONFIG to .amazon_reviews-config (absolute path)")

REPO_LOCATION = config.get("common", "REPO_LOCATION")
S3_BUCKET = config.get("common", "S3_BUCKET")
BATCH_JAR = config.get("batch", "BATCH_JAR")

default_args = {
    "owner": "airflow",
    "depends_on_past": True,
    "wait_for_downstream": True,
    "start_date": days_ago(2),
    "email": ["airflow@example.com"],
    "email_on_failure": False,
    "email_on_retry": False,
    "retries": 1,
    "retry_delay": timedelta(minutes=5),
}

dag = DAG(
    "batch_amazon_reviews",
    default_args=default_args,
    description="Batch processing for Amazon Reviews",
    schedule_interval=timedelta(days=1),
    max_active_runs=1,
)

start_batch_pipeline = DummyOperator(task_id="start_batch_pipeline", dag=dag)

download_s3_jar = BashOperator(
    task_id="download_s3_jar",
    bash_command=f"aws s3 cp {os.path.join(S3_BUCKET, BATCH_JAR)} {REPO_LOCATION}",
    retries=3,
    dag=dag,
)

spark_submit = BashOperator(
    task_id="spark_submit",
    bash_command=os.path.join(REPO_LOCATION, "scripts/bin/batch_spark_submit.sh "),
    retries=3,
    dag=dag,
)

cleanup_s3_jar = BashOperator(
    task_id="cleanup_s3_jar",
    bash_command=f"rm {os.path.join(REPO_LOCATION, BATCH_JAR)}",
    retries=0,
    dag=dag,
)

end_batch_pipeline = DummyOperator(task_id="end_batch_pipeline", dag=dag)

start_batch_pipeline >> download_s3_jar >> spark_submit >> cleanup_s3_jar >> end_batch_pipeline
