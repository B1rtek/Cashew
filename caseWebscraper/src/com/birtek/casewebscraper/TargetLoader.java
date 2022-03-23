package com.birtek.casewebscraper;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class TargetLoader {
    ArrayList<String> targets;

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
            System.err.println("Missing targets.txt");
        }
    }

    ArrayList<String> getTargets() {
        return this.targets;
    }
}
