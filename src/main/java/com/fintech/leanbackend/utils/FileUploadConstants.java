package com.fintech.leanbackend.utils;

import java.util.Set;
public final class FileUploadConstants {
    private FileUploadConstants() {
    }
    public static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

}
