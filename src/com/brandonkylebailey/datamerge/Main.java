package com.brandonkylebailey.datamerge;


import com.brandonkylebailey.datamerge.controllers.IngestController;
import com.brandonkylebailey.datamerge.controllers.ResourceManager;

import java.io.IOException;


public class Main {

    public static void main(String[] args) throws IOException {

        ResourceManager resourceManager = new ResourceManager();
        IngestController ingestController = new IngestController();

        resourceManager.prepareResources();
        ingestController.processData();

    }

}
