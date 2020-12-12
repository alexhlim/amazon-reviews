"""
Generates sample data from amazon-reviews-data.csv.
This is not to be called directly. Use scripts/bin/generate_data.sh.
"""
import sys

import pandas as pd

repo_loc = sys.argv[1]
try:
    sample_size = int(sys.argv[2])
except ValueError:
    print("SAMPLE_DATA_SIZE must be an int")
    sys.exit(1)


data = pd.read_csv(f"{repo_loc}/data/1429_1.csv.zip", sep=",", low_memory=False)
# Generating sample review ids for each product
data["reviews.id"] = data.groupby("id").transform(
    lambda x: [i for i in range(1, len(x.index) + 1)]
)
amazon_reviews_data_path = f"{repo_loc}/data/amazon-reviews-data.csv"
sample_data_path = f"{repo_loc}/data/sample.csv"
data.to_csv(amazon_reviews_data_path, index=False)
print(f"Full dataset: {amazon_reviews_data_path}")
data.iloc[:sample_size].to_csv(sample_data_path, index=False)
print(f"Sample dataset: {sample_data_path}")
