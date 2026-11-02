package com.fintech.leanbackend.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Imageutils {
    @Value("${file.upload-dir}")
    private final String uploadDir;

    public Imageutils(@Value("${file.upload-dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }
    public String getUploadDir() {
        return uploadDir;
    }
}
