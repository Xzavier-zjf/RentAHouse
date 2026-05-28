package com.example.rentalcommon.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public final class FileUploadValidator {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private FileUploadValidator() {
    }

    public static void requireValidImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("图片大小不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("仅支持 JPG、PNG、WEBP 或 GIF 图片");
        }
        if (!looksLikeImage(file, contentType.toLowerCase())) {
            throw new IllegalArgumentException("图片内容与文件类型不匹配");
        }
    }

    private static boolean looksLikeImage(MultipartFile file, String contentType) throws IOException {
        byte[] header = new byte[12];
        int read;
        try (InputStream inputStream = file.getInputStream()) {
            read = inputStream.read(header);
        }
        if (read < 4) {
            return false;
        }
        return switch (contentType) {
            case "image/jpeg" -> (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8;
            case "image/png" -> read >= 8
                    && (header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47;
            case "image/gif" -> header[0] == 0x47 && header[1] == 0x49 && header[2] == 0x46;
            case "image/webp" -> read >= 12
                    && header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46
                    && header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50;
            default -> false;
        };
    }
}
