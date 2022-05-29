package com.kaavya.utils;

import okhttp3.MediaType;

public class FileToPost {
    private String name;
    private MediaType mediaType;
    private String fileName;
    private byte[] fileData;

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public FileToPost(String name, String fileName, MediaType mediaType, byte[] fileData) {
        this.name = name;
        this.fileName = fileName;
        this.mediaType = mediaType;
        this.fileData = fileData;
    }
}
