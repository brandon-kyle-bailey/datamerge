package com.brandonkylebailey.datamerge;


import com.brandonkylebailey.datamerge.controllers.IngestController;
import com.brandonkylebailey.datamerge.controllers.ResourceManager;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;


public class Main {

    public static void main(String[] args) throws IOException, ParseException, ParserConfigurationException, SAXException {

        ResourceManager resourceManager = new ResourceManager();
        IngestController ingestController = new IngestController();

        resourceManager.prepareResources();
        ingestController.processData();
        ingestController.generateServiceGuidReport();

    }

}
