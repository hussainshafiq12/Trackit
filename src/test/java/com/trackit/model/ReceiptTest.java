package com.trackit.model;

import com.trackit.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Receipt Domain Model & Attachment Validation Tests")
class ReceiptTest {

    @Test
    @DisplayName("Should create valid Receipt attachment with metadata")
    void testValidReceiptCreation() {
        Receipt receipt = new Receipt("/path/to/invoice.pdf", "invoice.pdf", 2048, "application/pdf");
        assertEquals("/path/to/invoice.pdf", receipt.getFilePath());
        assertEquals("invoice.pdf", receipt.getOriginalFileName());
        assertEquals(2048, receipt.getFileSize());
        assertEquals("2.0 KB", receipt.getFormattedSize());
        assertFalse(receipt.isImage());
    }

    @Test
    @DisplayName("Should identify image receipts correctly")
    void testIsImage() {
        Receipt png = new Receipt("/img/receipt.png", "receipt.png", 512, "image/png");
        assertTrue(png.isImage());

        Receipt jpg = new Receipt("/img/receipt.jpg", "receipt.jpg", 1024, "image/jpeg");
        assertTrue(jpg.isImage());
    }

    @Test
    @DisplayName("Should throw ValidationException when filePath or fileName is empty")
    void testEmptyPathsThrowValidationException() {
        assertThrows(ValidationException.class, () -> {
            new Receipt(null, "receipt.png", 500, "image/png");
        });

        assertThrows(ValidationException.class, () -> {
            new Receipt("/path/file.png", "   ", 500, "image/png");
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when fileSize <= 0 or exceeds 10MB limit")
    void testFileSizeBoundariesThrowValidationException() {
        assertThrows(ValidationException.class, () -> {
            new Receipt("/path/file.png", "file.png", 0, "image/png");
        });

        assertThrows(ValidationException.class, () -> {
            new Receipt("/path/file.png", "file.png", -10, "image/png");
        });

        // Exceeds 10MB (10 * 1024 * 1024 + 1)
        long tooLarge = (10L * 1024 * 1024) + 1;
        assertThrows(ValidationException.class, () -> {
            new Receipt("/path/file.png", "file.png", tooLarge, "image/png");
        });
    }

    @Test
    @DisplayName("Should throw ValidationException for unsupported file extensions")
    void testUnsupportedExtensionThrowsValidationException() {
        assertThrows(ValidationException.class, () -> {
            new Receipt("/malicious.exe", "malicious.exe", 1024, "application/octet-stream");
        });
    }
}
