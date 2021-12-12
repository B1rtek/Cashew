package com.birtek.cashew;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class KeyLoader {

    private String key;

    public KeyLoader() {

    }

    public boolean loadKey() {
        try {
            File keyFile = new File(".key");
            Scanner reader = new Scanner(keyFile);
            if (reader.hasNextLine()) {
                key = reader.nextLine();
            } else {
                return false;
            }
            reader.close();
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("The key file wasn't found.");
            return false;
        }
    }

    public String getKey() {
        return key;
    }
}
