package com.brandonkylebailey.datamerge.controllers;


import com.brandonkylebailey.datamerge.models.Constants;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;


public class IngestController {

    // private local scope variables used throughout controller instance.
    private static String[] header;

    /**
     * getter method to get the header string list for the current instance of the class.
     * @return header.
     */
    private static String[] getHeader() {
        return header;
    }

    /**
     * setter method to set the header string list for the current instance of the class.
     * @param  csvParser csv parser object with desired headers.
     */
    private static void setHeader(CSVParser csvParser) {
        header = Arrays.copyOf(csvParser.getHeaderMap().keySet().toArray(),
                csvParser.getHeaderMap().keySet().toArray().length,
                String[].class);
    }

    /**
     * Create a new buffer reader from the given input file.
     * @param inputPathString Input file's path.
     * @return Reader from given file. null otherwise.
     */
    private static Reader newBufferedReader(String inputPathString) {
        try {
            return Files.newBufferedReader(Paths.get(inputPathString));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create a new CSVParser from the given reader.
     * @param reader newBufferReader.
     * @return CSVParser from given reader with first record as header.
     */
    private static CSVParser newCsvParser(Reader reader) {
        try {
            return new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Initializes a new buffer reader from given file either in append mode or not.
     * @param outputPathString output file's path.
     * @param appendMode Boolean to initialize bufferWriter in append mode.
     * @return bufferedWriter new writer for given file, either in append mode of not.
     */
    private static BufferedWriter newBufferedWriter(String outputPathString, Boolean appendMode) {
        try {
            if(appendMode) {
                return Files.newBufferedWriter(Paths.get(outputPathString), StandardOpenOption.APPEND);
            } else {
                return Files.newBufferedWriter(Paths.get(outputPathString));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Initializes new CSVPrinter either with or without header format.
     * @param writer bufferedWriter of current file.
     * @param header optional String...N number of string arguments to provide as header.
     * @return CSVPrinter with either header formatted or not.
     */
    private static CSVPrinter newCsvPrinter(BufferedWriter writer, String... header) {
        try {
            if(header.length > 0) {
                return new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(header));
            } else {
                return new CSVPrinter(writer, CSVFormat.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * set and return the header values from the given csvParser.
     * @param csvParser CSVParser object of given csv file.
     * @return header string list of headers.
     */
    private static String[] extractHeaderFromInputCsv(CSVParser csvParser) {
        assert csvParser != null;
        setHeader(csvParser);
        return getHeader();
    }

    /**
     * Run the ingestion process.
     */
    public static void Run() {

        // initialize resource controller to manage resources.
        ResourceController resourceController = new ResourceController();

        // if any of the resource files don't exist, data cant be ingested, so exit.
        if(!resourceController.resourceFilesExist()) {
            System.out.println("Error: resource files do not exist. Have you cloned the repo correctly?");
            System.exit(-1);
        }

        // create output files.
        resourceController.createNewFile(Constants.OUTPUT_PATH);
        resourceController.createNewFile(Constants.SERVICE_REPORT_PATH);

        // Initialize controllers for different stages of application.
        CsvController csvController = new CsvController();
        JsonController jsonController = new JsonController();
        XmlController xmlController = new XmlController();
        SortController sortController = new SortController();
        ReportController reportController = new ReportController();

        // register the header used for the output csv file.
        Reader reader = newBufferedReader(Constants.CSV_PATH);
        CSVParser csvParser = newCsvParser(reader);
        String[] header = extractHeaderFromInputCsv(csvParser);

        // initialize over write writer for output csv file.
        BufferedWriter outputCsvOverWriter = newBufferedWriter(Constants.OUTPUT_PATH, false);

        // ingest csv file.
        csvController.ingestCsvFile(csvParser, newCsvPrinter(outputCsvOverWriter, header));

        // ingest json file.
        BufferedWriter jsonWriter = newBufferedWriter(Constants.OUTPUT_PATH, true);
        jsonController.ingestJsonFile(newBufferedReader(Constants.JSON_PATH), newCsvPrinter(jsonWriter), header);

        // ingest xml file.
        BufferedWriter xmlWriter = newBufferedWriter(Constants.OUTPUT_PATH, true);
        xmlController.ingestXmlFile(newCsvPrinter(xmlWriter), header);

        // sort the output csv file in ascending order.
        Reader sortReader = newBufferedReader(Constants.OUTPUT_PATH);
        sortController.setCsvRecordsList(Objects.requireNonNull(newCsvParser(sortReader)));
        sortController.sortCsvFile("request-time",
                Objects.requireNonNull(newCsvPrinter(newBufferedWriter(Constants.OUTPUT_PATH, false),
                        header)));

        // generate serviceGuid report csv file.
        reportController.generateServiceGuidReport("service-guid",
                sortController.getCsvRecordsList(),
                newCsvPrinter(newBufferedWriter(Constants.SERVICE_REPORT_PATH, false),
                        "service-guid", "reports"));
    }
}