package com.example.splitmate_delta.models.addphoto;

import java.util.List;

public class GeneratePresignedUrlsResponse {
    private List<PresignedUrlInfo> presignedUrls;

    public List<PresignedUrlInfo> getPresignedUrls() {
        return presignedUrls;
    }

    public void setPresignedUrls(List<PresignedUrlInfo> presignedUrls) {
        this.presignedUrls = presignedUrls;
    }

    public static class PresignedUrlInfo {
        private String fileName;
        private String presignedUrl;
        private String photoUrl;

        public String getFileName() {
            return fileName;
        }

        public String getPresignedUrl() {
            return presignedUrl;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public void setPresignedUrl(String presignedUrl) {
            this.presignedUrl = presignedUrl;
        }

        public void setPhotoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
        }
    }
}