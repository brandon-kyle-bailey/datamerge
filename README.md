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

- openjdk version "13.0.2" 2020-01-14
- OpenJDK Runtime Environment AdoptOpenJDK (build 13.0.2+8)
- Eclipse OpenJ9 VM AdoptOpenJDK (build openj9-0.18.0, JRE 13 Windows 10 amd64-64-Bit Compressed References 20200117_154 (JIT enabled, AOT enabled)


## Dependencies

- org.apache.commons-csv-1.6
- com.googlecode.json-simple-1.1.1

## Getting Started

```aidl
git clone https://github.com/brandon-kyle-bailey/datamerge.git
```

## How to run

```aidl
cd datamerge/out/artifacts/datamerge_jar

java -jar datamerge.jar
```

## Approach 

The application can be broken down in to 4 main points of focus:

- Manage resource files (Input data files, output files)
- Ingest data from input files.
- Sort data in output file.
- Generate report based on `service-guid` count. 

Immediately, we realize we need to manage three file types `csv`, `json` and `xml`.

## Libraries

When considering which tools to use, i wanted to make sure the application remained light weight, and that
development would be easy to pick up for someone inheriting this code base.

There for i chose to only use either open source or builtin libraries.

### CSV
To manage the CSV data, i have chosen to use Apaches commons-csv library. It is a simple and open source
library.

### JSON
To manage the JSON data, i have chosen to use `json-simple`. Because it is lightweight and open source.

### XML
To manage the XML data, i have chosen to use the builtin w3c dom library, along with `javax.xml`. 

## Points of conversation

### JSON Date format: 
An issue I encountered when building out this program was the format of the date in the json objects.
I noticed it was in milliseconds. This had to be converted to a human readable format before being added
to the output CSV, in the correct time zone. 

To achieve this, I used the `SimpleDateFormat` class to create a human readable date format `yyyy-MM-dd HH:mm:ss z` in `Canada/Atlantic` timezone.
  