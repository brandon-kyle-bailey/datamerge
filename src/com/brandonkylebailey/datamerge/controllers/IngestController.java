package com.brandonkylebailey.datamerge.controllers;


import com.brandonkylebailey.datamerge.models.Constants;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class IngestController {

    private String[] header;

    private void processCsvFile() throws IOException {

        // create buffer reader from input csv file.
        Reader reader = Files.newBufferedReader(Paths.get(Constants.CSV_PATH));

        // add each record from the input csv file to the output csv file.
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(Constants.OUTPUT_PATH));

        // create csv parser with headers set.
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

        // get headers for later use.
        header = Arrays.copyOf(csvParser.getHeaderMap().keySet().toArray(), csvParser.getHeaderMap().keySet().toArray().length, String[].class);

        // create csv printer for writing of records to output file. Provide header as header.
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(header));

        // loop through records and filter non zero "packets-serviced" records.
        for (CSVRecord record : csvParser) {
         if(!(Integer.parseInt(record.get("packets-serviced")) == 0)) {
             csvPrinter.printRecord(record.toMap().values());
             csvPrinter.flush();
         }
        }

        csvPrinter.close();
    }

    private void processJsonFile() {

        System.out.println(header);

    }

    private void processXmlFile() {

        System.out.println(header);

    }

    public void processData() throws IOException {
        processCsvFile();
        processJsonFile();
        processXmlFile();
    }
}