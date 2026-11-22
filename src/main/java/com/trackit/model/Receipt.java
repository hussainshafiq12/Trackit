package com.trackit.model;

import com.trackit.exception.ValidationException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Receipt - Represents a receipt attachment for an expense.
 * Stores file path and metadata, enforcing boundary and extension validation.
 */
public class Receipt implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "png", "jpg", "jpeg");
    
    private String filePath;
    private String originalFileName;
    private long fileSize;
    private String mimeType;
    
    public Receipt(String filePath, String originalFileName, long fileSize, String mimeType) {
        validate(filePath, originalFileName, fileSize);
        
        this.filePath = filePath.trim();
        this.originalFileName = originalFileName.trim();
        this.fileSize = fileSize;
        this.mimeType = mimeType != null ? mimeType.trim() : "application/octet-stream";
    }
    
    private void validate(String filePath, String originalFileName, long fileSize) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new ValidationException("Receipt file path cannot be empty");
        }
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new ValidationException("Receipt file name cannot be empty");
        }
        if (fileSize <= 0) {
            throw new ValidationException("Receipt file size must be strictly positive");
        }
        if (fileSize > MAX_FILE_SIZE_BYTES) {
            throw new ValidationException("Receipt file size exceeds maximum 10MB limit: " + fileSize + " bytes");
        }
        
        String ext = getFileExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase(Locale.ROOT))) {
            throw new ValidationException("Unsupported receipt file format: " + ext + ". Allowed: " + ALLOWED_EXTENSIONS);
        }
    }
    
    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
    
    public boolean isImage() {
        String ext = getFileExtension(originalFileName).toLowerCase(Locale.ROOT);
        return ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || 
               (mimeType != null && mimeType.startsWith("image/"));
    }
    
    // Getters and Setters
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new ValidationException("Receipt file path cannot be empty");
        }
        this.filePath = filePath.trim();
    }
    
    public String getOriginalFileName() {
        return originalFileName;
    }
    
    public void setOriginalFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new ValidationException("Receipt file name cannot be empty");
        }
        this.originalFileName = originalFileName.trim();
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        if (fileSize <= 0 || fileSize > MAX_FILE_SIZE_BYTES) {
            throw new ValidationException("Invalid receipt file size: " + fileSize);
        }
        this.fileSize = fileSize;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    /**
     * Get human-readable file size
     */
    public String getFormattedSize() {
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
    }
}
