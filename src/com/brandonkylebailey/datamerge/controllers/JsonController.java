package com.brandonkylebailey.datamerge.controllers;


import org.apache.commons.csv.CSVPrinter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class JsonController {

    /**
     * Ingest a json file to a given output csv file, maintaining header order.
     * @param reader Buffer reader of input file.
     * @param csvPrinter CSVPrinter object of output csv file.
     * @param header String array of headers.
     */
    public void ingestJsonFile(Reader reader, CSVPrinter csvPrinter ,String[] header) {

        try {

            JSONParser jsonParser = new JSONParser();
            Object obj = jsonParser.parse(reader);
            JSONArray reports = (JSONArray) obj;
            // Process reports.
            for (Object rawReport : reports) {

                // Turn each report into a JSON Object.
                JSONObject report = (JSONObject) rawReport;

                // create array to store each record
                ArrayList<String> row = new ArrayList<>();

                // filter non zero packets serviced records.
                if((((Long)report.get("packets-serviced")).intValue() != 0)) {

                    // append each column in the same order as the corresponding header.
                    for (String column : header) {

                        if(column.equals("request-time")) {

                            row.add(convertMillisecondsToDate(report.get(column),
                                    "Canada/Atlantic",
                                    "yyyy-MM-dd HH:mm:ss z"));
                        } else {
                            row.add(report.get(column).toString());
                        }
                    }
                    // add row to csv file.
                    csvPrinter.printRecord(row);
                    csvPrinter.flush();
                }
            }
            csvPrinter.close();
            System.out.println("Json file processed.");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }

    /**
     * Converts JSON files milliseconds value to a valid date object.
     * @param columnValue value object of current column.
     * @param timeZone String of desired timezone for date output.
     * @param format String of format given date should be in.
     * @return String of formatted date.
     */
    private String convertMillisecondsToDate(Object columnValue, String timeZone, String format) {

        long milliseconds = (long) columnValue;
        TimeZone astTimeZone = TimeZone.getTimeZone(timeZone);
        DateFormat dateTimeFormat = new SimpleDateFormat(format);
        dateTimeFormat.setTimeZone(astTimeZone);
        return dateTimeFormat.format(new Date(milliseconds));
    }
}
