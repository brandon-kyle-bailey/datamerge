package com.brandonkylebailey.datamerge.controllers;


import com.brandonkylebailey.datamerge.models.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ResourceManager {

    private boolean inputFileExists(String inputFile) {

        return new File(inputFile).exists();
    }

    private void createNewFile(String inputFile) {
        if(!inputFileExists(inputFile)) {
            File file = new File(inputFile);
            try {
                if (file.createNewFile()){
                    System.out.println("Output file created successfully.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Output file already exists.");
        }
    }

    public void prepareResources() throws FileNotFoundException {

        // check each file exists
        if(!inputFileExists(Constants.CSV_PATH) || !inputFileExists(Constants.JSON_PATH) || !inputFileExists(Constants.XML_PATH)) {
            throw new FileNotFoundException();
        }
        createNewFile(Constants.OUTPUT_PATH);
        createNewFile(Constants.SERVICE_REPORT_PATH);
    }
}
