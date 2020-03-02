package com.brandonkylebailey.datamerge.controllers;


import com.brandonkylebailey.datamerge.models.Constants;

import java.io.File;
import java.io.IOException;


public class ResourceManager {

    /**
     * Boolean method that checks if given file exists or not.
     * @param inputFilePathString Input file's path.
     * @return true if file exists, false if not.
     */
    private boolean givenFileExists(String inputFilePathString) {
        return new File(inputFilePathString).exists();
    }

    /**
     * Tries to create a new file. Catches IOException if exception is thrown.
     * @param inputFilePathString Input file's path.
     */
    public void createNewFile(String inputFilePathString) {

        // Initialize File using input string.
        File newFile = new File(inputFilePathString);

        // try to make the new file in the location, catch IOException and print stack trace if raised.
        try {
            if(newFile.createNewFile()) {
                System.out.println(String.format("File: %s created successfully.", inputFilePathString));
            } else {
                System.out.println(String.format("File: %s was not created.", inputFilePathString));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Boolean method to check if all input files exist.
     * @return true if files exist, false if not.
     */
    public Boolean resourceFilesExist() {

        if(givenFileExists(Constants.CSV_PATH) && givenFileExists(Constants.JSON_PATH) && givenFileExists(Constants.XML_PATH)) {
            return true;
        } else {
            return false;
        }
    }
}
