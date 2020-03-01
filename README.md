# Data sorting and filtering

Read the 3 input files reports.json, reports.csv, reports.xml and output a combined CSV file with the following characteristics:

- The same column order and formatting as reports.csv
- All report records with packets-serviced equal to zero should be excluded
- records should be sorted by request-time in ascending order

Additionally, the application should print a summary showing the number of records in the output file associated with each service-guid.

Please provide source, documentation on how to run the program and an explanation on why you chose the tools/libraries used.

## Submission

You may fork this repo, commit your work and let us know of your project's location, or you may email us your project files in a zip file.

## Requirements

- something

## Dependencies

- something

## Getting Started

- something

## How to run

- something

## Initial Thoughts

The requirements for the this project can be broken down in to `5 steps`:
- read/ `ingest` the `data`.
- `filter` to `exclude` records with the value of `packets-serviced` equal to `zero`.
- create `summary` showing the `number of records` associated with each `service-guid`.
- `sort data` in `ascending order` based on the `request-time`.
- `output` a `csv` file `merged_reports.csv` of merged/ ingested data with `identical header formatting`.

## Initial Approach 

As the data is consistent between files, i feel it is appropriate to take a linear approach. 
Process each file in concession, filtering out records that don't meet the requirements and writing
to the output csv file as i move through each file.

Beginning with the csv file would be wise as the output file will be of the same type and as the header is 
needed, for consistency, it seems to be the logical approach.

