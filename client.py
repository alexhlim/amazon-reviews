import sys

import pandas as pd
import requests

url = sys.argv[1]
csv = sys.argv[2]

# low_memory -> ensure no mixed dtypes
data = pd.read_csv(csv, sep=",", low_memory=False)

# Write all the rows from sample data to the api as a post request
for i in data.index:
    try:
        export = data.loc[i].to_json()
        response = requests.post(url, data=export)
        print(export)
        print(response)
    except ValueError:
        print(data.loc[i])
