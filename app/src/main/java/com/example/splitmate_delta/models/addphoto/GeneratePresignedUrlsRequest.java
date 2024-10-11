package com.example.splitmate_delta.models.addphoto;

import java.util.List;

public class GeneratePresignedUrlsRequest {
    private List<String> fileNames;

    public GeneratePresignedUrlsRequest(List<String> fileNames) {
        this.fileNames = fileNames;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }
}