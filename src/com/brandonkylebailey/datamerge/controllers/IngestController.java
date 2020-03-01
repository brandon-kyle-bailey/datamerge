package com.brandonkylebailey.datamerge.controllers;


import com.brandonkylebailey.datamerge.models.Constants;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class IngestController {

    private String[] header;
    private ArrayList<CSVRecord> csvRecordsList;

    private void processCsvFile() throws IOException {

        // create buffer reader from input csv file.
        Reader reader = Files.newBufferedReader(Paths.get(Constants.CSV_PATH));

        // create csv parser with headers set.
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

        // add each record from the input csv file to the output csv file.
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(Constants.OUTPUT_PATH));

        // get headers for later use.
        header = Arrays.copyOf(csvParser.getHeaderMap().keySet().toArray(),
                csvParser.getHeaderMap().keySet().toArray().length,
                String[].class);

        // create csv printer for writing of records to output file. Provide header as header.
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(header));

        // loop through records and filter non zero "packets-serviced" records.
        for (CSVRecord record : csvParser) {
            if((Integer.parseInt(record.get("packets-serviced")) != 0)) {
                // TODO... is there a more efficient way to generate this list?
                ArrayList<String> row = new ArrayList<>();
                record.forEach(row::add);
                csvPrinter.printRecord(row);
                csvPrinter.flush();
         }
        }

        csvPrinter.close();
    }

    private void processJsonFile() throws IOException, ParseException {

        // create buffer reader from json file.
        Reader reader = Files.newBufferedReader(Paths.get(Constants.JSON_PATH));

        // create json parser to read the json file.
        JSONParser jsonParser = new JSONParser();

        //Read the file.
        Object obj = jsonParser.parse(reader);

        // generate json array of report records.
        JSONArray reports = (JSONArray) obj;

        // create buffer reader in append mode to add to the csv
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(Constants.OUTPUT_PATH), StandardOpenOption.APPEND);

        // create new csv printer. header already present in file at this point, so no header needed.
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

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
                    if(column.toString().equals("request-time")) {
                        // convert milliseconds to date object
                        long milliseconds = (long) report.get(column);

                        // generate Atlantic Standard Time zone.
                        TimeZone astTimeZone = TimeZone.getTimeZone("Canada/Atlantic");
                        DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                        dateTimeFormat.setTimeZone(astTimeZone);

                        Date result = new Date(milliseconds);
                        row.add(dateTimeFormat.format(result));
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
    }

    private void processXmlFile() throws ParserConfigurationException, IOException, SAXException {

        // Read xml file from input file.
        File fXmlFile = new File(Constants.XML_PATH);

        // create the document builder.
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();

        // create the csv writer in append mode.
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(Constants.OUTPUT_PATH), StandardOpenOption.APPEND);

        // create new csv printer from the writer without the header as it already exists in the file.
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

        // Parse red xml file and normalize.
        Document document = documentBuilder.parse(fXmlFile);
        document.getDocumentElement().normalize();

        // create a node list of reports
        NodeList nList = document.getElementsByTagName("report");

        // Process each report.
        for (int idx = 0; idx < nList.getLength(); idx++) {
            // create array list for row
            ArrayList<String> row = new ArrayList<>();

            // create node from report
            Node reportNode = nList.item(idx);

            // create element from node
            Element element = (Element) reportNode;

            // filter non zero packets-serviced
            if(Integer.parseInt(element.getElementsByTagName("packets-serviced").item(0).getTextContent()) != 0) {

                // add each column to the array in order of header.
                for (String column : header) {
                    row.add(element.getElementsByTagName(column).item(0).getTextContent());
                }

                // add row to csv file
                csvPrinter.printRecord(row);
                csvPrinter.flush();
            }

        }
        // Close printer.
        csvPrinter.close();
    }

    private void sortOutputFile() throws IOException {

        String field = "request-time";

        Reader reader = Files.newBufferedReader(Paths.get(Constants.OUTPUT_PATH));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());

        csvRecordsList = new ArrayList<>();
        for (CSVRecord csvRecord: csvParser) { csvRecordsList.add(csvRecord); }

        Comparator<CSVRecord> comparator = (op1, op2) -> String.valueOf(op2.get(field)).compareTo(String.valueOf(op1.get(field)));
        csvRecordsList.sort(comparator);

        // add each record from the input csv file to the output csv file.
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(Constants.OUTPUT_PATH));
        // create csv printer for writing of records to output file. Provide header as header.
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(header));
        csvPrinter.printRecords(csvRecordsList);
        csvPrinter.flush();
        csvPrinter.close();
    }

    public void generateServiceGuidReport() throws IOException {

        String column = "service-guid";

        // Create treeMap to store "service-guid" reports per each guid as value.
        TreeMap<String, Integer> serviceGuidSummaries = new TreeMap<>();

        // Process each report.
        for (CSVRecord record : csvRecordsList) {
            // If the "service-guid" has been already inserted in the treeMap, update it value.
            if (serviceGuidSummaries.containsKey(record.get(column))) {
                int prevValue = serviceGuidSummaries.get(record.get(column));
                serviceGuidSummaries.put(record.get(column), prevValue + 1);
            }
            // If the "service-guid" has never been in the treeMap, insert with value 1.
            else {
                serviceGuidSummaries.put(record.get(column), 1);
            }
        }

        BufferedWriter writer = Files.newBufferedWriter(Paths.get(Constants.SERVICE_REPORT_PATH));

        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(column, "reports"));

        for (String key: serviceGuidSummaries.keySet()) {
            // Print record in output file and flush.
            csvPrinter.printRecord(key, serviceGuidSummaries.get(key));
            csvPrinter.flush();
        }
        // Close printer.
        csvPrinter.close();

    }

    public void processData() throws IOException, ParseException, ParserConfigurationException, SAXException {
        processCsvFile();
        System.out.println("Csv file processed.");
        processJsonFile();
        System.out.println("Json file processed.");
        processXmlFile();
        System.out.println("Xml file processed.");
        sortOutputFile();
        System.out.println("Output csv file sorted.");
    }
}