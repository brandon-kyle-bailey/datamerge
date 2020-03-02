package com.brandonkylebailey.datamerge.controllers;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.ArrayList;

public class CsvController {

    /**
     * Ingest a csv file to a given output csv file.
     * @param csvParser CSVParser object of input csv file.
     * @param csvPrinter CSVPrinter object of output csv file.
     */
    public void ingestCsvFile(CSVParser csvParser, CSVPrinter csvPrinter) {
        try {
            // loop through records and filter non zero "packets-serviced" records.
            for (CSVRecord record : csvParser) {
                if((Integer.parseInt(record.get("packets-serviced")) != 0)) {
                    ArrayList<String> row = new ArrayList<>();
                    record.forEach(row::add);
                    csvPrinter.printRecord(row);
                    csvPrinter.flush();
                }
            }
            csvPrinter.close();
            System.out.println("Csv file processed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
