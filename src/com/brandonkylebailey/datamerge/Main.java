package com.brandonkylebailey.datamerge;


import com.brandonkylebailey.datamerge.controllers.IngestController;
import com.brandonkylebailey.datamerge.controllers.ResourceManager;
import com.brandonkylebailey.datamerge.models.Constants;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;


public class Main {

    public static void main(String[] args) throws ParserConfigurationException, SAXException, ParseException, IOException {

        // initialize resource manager.
        ResourceManager resourceManager = new ResourceManager();

        // sanity check resource files.
        if(!resourceManager.resourceFilesExist()) {
            System.out.println("Error: resource files do not exist. Have you cloned the repo correctly?");
            return;
        }

        // create output csv files needed.
        resourceManager.createNewFile(Constants.OUTPUT_PATH);
        resourceManager.createNewFile(Constants.SERVICE_REPORT_PATH);

        // initialize ingest controller to run methods on input files.
        IngestController ingestController = new IngestController();

        // process ingest data.
        ingestController.processData();

        // generate report csv.
        ingestController.generateServiceGuidReport();

    }

}
