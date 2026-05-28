package com.example.rentalcommon.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileUploadValidatorTest {

    @Test
    void shouldAcceptValidPng() {
        byte[] pngHeader = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        MockMultipartFile file = new MockMultipartFile("file", "ok.png", "image/png", pngHeader);
        assertDoesNotThrow(() -> FileUploadValidator.requireValidImage(file));
    }

    @Test
    void shouldRejectInvalidContentType() {
        MockMultipartFile file = new MockMultipartFile("file", "bad.txt", "text/plain", "abc".getBytes());
        assertThrows(IllegalArgumentException.class, () -> FileUploadValidator.requireValidImage(file));
    }

    @Test
    void shouldRejectHeaderMismatch() {
        MockMultipartFile file = new MockMultipartFile("file", "fake.png", "image/png", "not-png".getBytes());
        assertThrows(IllegalArgumentException.class, () -> FileUploadValidator.requireValidImage(file));
    }

    @Test
    void shouldRejectOversizedFile() {
        byte[] huge = new byte[5 * 1024 * 1024 + 1];
        huge[0] = (byte) 0xFF;
        huge[1] = (byte) 0xD8;
        MockMultipartFile file = new MockMultipartFile("file", "huge.jpg", "image/jpeg", huge);
        assertThrows(IllegalArgumentException.class, () -> FileUploadValidator.requireValidImage(file));
    }

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> FileUploadValidator.requireValidImage(file));
    }
}

