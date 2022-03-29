package com.birtek.casewebscraper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class TargetLoader {
    ArrayList<String> targets;
    private static final Logger LOGGER = LoggerFactory.getLogger(TargetLoader.class);


    public TargetLoader() {
        targets = new ArrayList<>();
    }

    void load() {
        try {
            File targetsFile = new File("targets.txt");
            Scanner reader = new Scanner(targetsFile);
            while (reader.hasNextLine()) {
                targets.add(reader.nextLine());
            }
            reader.close();
        } catch (FileNotFoundException e) {
            LOGGER.error("Missing targets.txt");
        }
    }

    ArrayList<String> getTargets() {
        return this.targets;
    }
}
