package com.birtek.cashew.badwayfarersubmissions;

public class NewSubmission {

    private final String fileID;
    private String description;
    public NewSubmission(String fileID) {
        this.fileID = fileID;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileID() {
        return fileID;
    }

    public String getDescription() {
        return description;
    }
}
