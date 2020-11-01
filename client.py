import pandas as pd
import requests

url = "https://xxxxxxxxxx.execute-api.us-east-2.amazonaws.com/dev/test"

# low_memory -> ensure no mixed dtypes
data = pd.read_csv("sample.csv", sep=",", low_memory=False)

# Write all the rows from sample data to the api as a post request
for i in data.index:
    try:
        export = data.loc[i].to_json()
        response = requests.post(url, data=export)
        print(export)
        print(response)
    except ValueError:
        print(data.loc[i])
