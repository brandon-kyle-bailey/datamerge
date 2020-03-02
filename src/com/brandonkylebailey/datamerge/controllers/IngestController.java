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

    // private local scope variables used throughout controller instance.
    private String[] header;
    private ArrayList<CSVRecord> csvRecordsList;

    /**
     * Create a new file.
     * @param inputPathString Input file's path.
     * @return Reader from given file.
     * @throws IOException if newBufferReader cant be created.
     */
    private Reader newBufferedReader(String inputPathString) throws IOException {
        return Files.newBufferedReader(Paths.get(inputPathString));
    }

    /**
     * Initialize a new CSVParser
     * @param reader newBufferReader.
     * @return CSVParser from given reader with first record as header.
     * @throws IOException if CSVParser cant be created.
     */
    private CSVParser newCsvParser(Reader reader) throws IOException {
        return new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
    }

    /**
     * Copies header from initial reports.csv file to block scope header variable for later use.
     * @param csvParser Instance of input csv file.
     */
    private void copyHeaders(CSVParser csvParser) {
        header = Arrays.copyOf(csvParser.getHeaderMap().keySet().toArray(), csvParser.getHeaderMap().keySet().toArray().length, String[].class);
    }

    /**
     * Initializes a new buffer reader from given file either in append mode or not.
     * @param outputPathString output file's path.
     * @param appendMode Boolean to initialize bufferWriter in append mode.
     * @return bufferedWriter new writer for given file, either in append mode of not.
     * @throws IOException if newBufferWriter cant be created.
     */
    private BufferedWriter newBufferedWriter(String outputPathString, Boolean appendMode) throws IOException {

        if(appendMode) {
            return Files.newBufferedWriter(Paths.get(outputPathString), StandardOpenOption.APPEND);
        } else {
            return Files.newBufferedWriter(Paths.get(outputPathString));
        }
    }

    /**
     * Initializes new CSVPrinter either with or without header format.
     * @param writer bufferedWriter of current file.
     * @param header optional String...N number of string arguments to provide as header.
     * @return CSVPrinter with either header formatted or not.
     * @throws IOException if CSVPrinter cant be initialized.
     */
    private CSVPrinter newCsvPrinter(BufferedWriter writer, String... header) throws IOException {
        if(header.length > 0) {
            return new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(header));
        } else {
            return new CSVPrinter(writer, CSVFormat.DEFAULT);
        }
    }

    /**
     * Processes the contents of the CSV file.
     * @throws IOException if  objects cant be created at any stage.
     */
    private void processCsvFile() throws IOException {

        // create buffer reader from input csv file.
        Reader reader = newBufferedReader(Constants.CSV_PATH);

        // create csv parser with headers set.
        CSVParser csvParser = newCsvParser(reader);

        // copy headers to local variable for use else where in class instance.
        copyHeaders(csvParser);

        // add each record from the input csv file to the output csv file.
        BufferedWriter writer = newBufferedWriter(Constants.OUTPUT_PATH, false);

        // create csv printer for writing of records to output file. Provide header as header.
        CSVPrinter csvPrinter = newCsvPrinter(writer, header);

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

    /**
     * Process contents of JSON file.
     * @param reports JSONArray of json file contents.
     * @param csvPrinter CSVPrinter of printer for output CSV file.
     * @throws IOException if CSVPrinter cant be manipulated.
     */
    private void processJsonData(JSONArray reports, CSVPrinter csvPrinter) throws IOException {

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

                        row.add(convertMillisecondsToDate(report.get(column), "Canada/Atlantic", "yyyy-MM-dd HH:mm:ss z"));
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

    /**
     * Process the JSON file.
     * @throws IOException if bufferReader, bufferWriter or JSON data cant be accessed.
     * @throws ParseException if JsonParser cant parse the bufferReader.
     */
    private void processJsonFile() throws IOException, ParseException {

        // create buffer reader from json file.
        Reader reader = newBufferedReader(Constants.JSON_PATH);

        // create json parser to read the json file.
        JSONParser jsonParser = new JSONParser();

        //Read the file.
        Object obj = jsonParser.parse(reader);

        // generate json array of report records.
        JSONArray reports = (JSONArray) obj;

        // create buffer reader in append mode to add to the csv
        BufferedWriter writer = newBufferedWriter(Constants.OUTPUT_PATH, true);

        CSVPrinter csvPrinter = newCsvPrinter(writer);

        processJsonData(reports, csvPrinter);
        System.out.println("Json file processed.");
    }

    /**
     * Process the XML data.
     * @param nodeList NodeList of XML element nodes.
     * @param csvPrinter CSVPrinter of output file.
     * @throws IOException if csvPrinter cant be accessed.
     */
    private void processXmlData(NodeList nodeList, CSVPrinter csvPrinter) throws IOException {
        // Process each report.
        for (int idx = 0; idx < nodeList.getLength(); idx++) {
            // create array list for row
            ArrayList<String> row = new ArrayList<>();
            // create node from report
            Node reportNode = nodeList.item(idx);
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

    /**
     * Process the XML file.
     * @throws ParserConfigurationException if Document builder cant be invoked.
     * @throws IOException if newBufferedWriter or newCsvPrinter cant be accessed.
     * @throws SAXException if xml file instance cant be parsed to document builder.
     */
    private void processXmlFile() throws ParserConfigurationException, IOException, SAXException {

        // Read xml file from input file.
        File fXmlFile = new File(Constants.XML_PATH);

        // create the document builder.
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();

        // create the csv writer in append mode.
        BufferedWriter writer = newBufferedWriter(Constants.OUTPUT_PATH, true);

        // create new csv printer from the writer without the header as it already exists in the file.
        CSVPrinter csvPrinter = newCsvPrinter(writer);

        // Parse red xml file and normalize.
        Document document = documentBuilder.parse(fXmlFile);
        document.getDocumentElement().normalize();

        // create a node list of reports
        NodeList nodeList = document.getElementsByTagName("report");

        processXmlData(nodeList, csvPrinter);
        System.out.println("Xml file processed.");
    }

    /**
     * Dumps contents of output CSV file in to ArrayList.
     * @param csvParser parser instance of output CSV file.
     */
    private void createRecordArray(CSVParser csvParser) {
        csvRecordsList = new ArrayList<>();
        for (CSVRecord csvRecord: csvParser) {
            csvRecordsList.add(csvRecord);
        }
    }

    /**
     * Using a comparator, sort the ArrayList of csv records.
     * @param field String of given field to base sort from.
     */
    private void sortRecordArray(String field) {
        Comparator<CSVRecord> comparator = (op1, op2) -> String.valueOf(op2.get(field)).compareTo(String.valueOf(op1.get(field)));
        csvRecordsList.sort(comparator);
    }

    /**
     * Sort the output CSV file in ascending order based on request-time.
     * @throws IOException if buffer or csv printer cant be accessed.
     */
    private void sortOutputFile() throws IOException {

        String field = "request-time";

        // Reader reader = Files.newBufferedReader(Paths.get(Constants.OUTPUT_PATH));
        Reader reader = newBufferedReader(Constants.OUTPUT_PATH);

        // CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
        CSVParser csvParser = newCsvParser(reader);

        createRecordArray(csvParser);
        sortRecordArray(field);

        // add each record from the input csv file to the output csv file.
        BufferedWriter writer = newBufferedWriter(Constants.OUTPUT_PATH, false);

        // create csv printer for writing of records to output file. Provide header as header.
        CSVPrinter csvPrinter = newCsvPrinter(writer, header);

        csvPrinter.printRecords(csvRecordsList);
        csvPrinter.flush();
        csvPrinter.close();
        System.out.println("Output csv file sorted.");
    }

    /**
     * Generate the report CSV file using the service-guid column.
     * @throws IOException if bufferWriter or csvPrinter cant be accessed.
     */
    public void generateServiceGuidReport() throws IOException {

        String column = "service-guid";

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

        BufferedWriter writer = newBufferedWriter(Constants.SERVICE_REPORT_PATH, false);

        CSVPrinter csvPrinter = newCsvPrinter(writer, column, "reports");

        for (String key: serviceGuidSummaries.keySet()) {
            // Print record in output file and flush.
            csvPrinter.printRecord(key, serviceGuidSummaries.get(key));
            csvPrinter.flush();
        }
        // Close printer.
        csvPrinter.close();
        System.out.println("Service Guid report generated.");
    }

    /**
     * Processes the resource files.
     * @throws IOException if any function cant be accessed.
     * @throws ParseException if the jsonFile method cant be parsed.
     * @throws ParserConfigurationException if the xml file cant be configured.
     * @throws SAXException if the xml cant be parsed to builder.
     */
    public void processData() throws IOException, ParseException, ParserConfigurationException, SAXException {
        processCsvFile();
        processJsonFile();
        processXmlFile();
        sortOutputFile();
    }
}