package com.brandonkylebailey.datamerge.controllers;


import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class SortController {

    // array of all records in csv file for sorting.
    private ArrayList<CSVRecord> csvRecordsList = new ArrayList<>();

    /**
     * Method to add records to array list to sort before writing back to csv file.
     * @param csvParser CSVParser of given csv file.
     */
    public void setCsvRecordsList(CSVParser csvParser) {
        for (CSVRecord csvRecord: csvParser) {
            csvRecordsList.add(csvRecord);
        }
    }

    /**
     * getter method to get the record list.
     * @return this.csvRecordsList ArrayList<CSVRecord> of records.
     */
    public ArrayList<CSVRecord> getCsvRecordsList() {
        return this.csvRecordsList;
    }

    /**
     * Method to sort the csv file.
     * @param field string of desired field to base sorting from.
     * @param csvPrinter CSVPrinter object of output csv file.
     */
    public void sortCsvFile(String field, CSVPrinter csvPrinter) {
        try {

            // comparator lambda to sort array.
            Comparator<CSVRecord> comparator = (op1, op2) -> String.valueOf(op2.get(field)).compareTo(String.valueOf(op1.get(field)));
            csvRecordsList.sort(comparator);

            // write records to csv file.
            csvPrinter.printRecords(csvRecordsList);
            csvPrinter.flush();
            csvPrinter.close();
            System.out.println("Output csv file sorted.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
