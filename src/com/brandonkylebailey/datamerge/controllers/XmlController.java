package com.brandonkylebailey.datamerge.controllers;

import com.brandonkylebailey.datamerge.models.Constants;
import org.apache.commons.csv.CSVPrinter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class XmlController {

    /**
     * Ingest the contents of an xml file into the output csv file.
     * @param csvPrinter CSVPrinter object of output csv file.
     * @param header string list of headers to maintain header order.
     */
    public void ingestXmlFile(CSVPrinter csvPrinter, String[] header) {
        try {

            // create file instance from xml path.
            File fXmlFile = new File(Constants.XML_PATH);

            // create the document builder.
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(fXmlFile);
            // normalize xml data.
            document.getDocumentElement().normalize();

            // create node list of xml elements.
            NodeList nodeList = document.getElementsByTagName("report");

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
            System.out.println("Xml file processed.");
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
    }

}
