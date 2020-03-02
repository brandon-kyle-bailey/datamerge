package com.brandonkylebailey.datamerge.controllers;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

public class ReportController {

    /**
     * Generate the report csv file for hte service-guid occurances.
     * @param column string of desired report column
     * @param csvRecordsList array of given CSVRecord objects to base report from.
     * @param csvPrinter CSVPrinter object of output csv file.
     */
    public void generateServiceGuidReport(String column, ArrayList<CSVRecord> csvRecordsList, CSVPrinter csvPrinter) {
        try {
            // String column = "service-guid";

            // Create treeMap to store "service-guid" reports per each guid as value.
            TreeMap<String, Integer> serviceGuidSummaries = new TreeMap<>();

            // Process each report.
            for (CSVRecord record : csvRecordsList) {
                // If the "service-guid" has been already inserted in the treeMap, update it value.
                if (serviceGuidSummaries.containsKey(record.get(column))) {
                    int currentValue = serviceGuidSummaries.get(record.get(column));
                    serviceGuidSummaries.put(record.get(column), currentValue + 1);
                }
                // If the "service-guid" has never been in the treeMap, insert with value 1.
                else {
                    serviceGuidSummaries.put(record.get(column), 1);
                }
            }
            for (String key: serviceGuidSummaries.keySet()) {
                // Print record in output file and flush.
                csvPrinter.printRecord(key, serviceGuidSummaries.get(key));
                csvPrinter.flush();
            }
            // Close printer.
            csvPrinter.close();
            System.out.println("Service Guid report generated.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
