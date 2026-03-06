package com.example.helloworld;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * REST Controller for log file upload and comparison endpoints
 */
@RestController
@RequestMapping("/api/logs")
public class LogUploadController {

    private static final String UPLOAD_DIR = "uploaded_logs";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    /**
     * Upload log file v1
     *
     * @param file MultipartFile containing the log file
     * @return Response with upload status
     */
    @PostMapping("/upload/v1")
    public ResponseEntity<Map<String, Object>> uploadLogV1(@RequestParam("file") MultipartFile file) {
        return uploadLog(file, "log_v1.txt");
    }

    /**
     * Upload log file v2
     *
     * @param file MultipartFile containing the log file
     * @return Response with upload status
     */
    @PostMapping("/upload/v2")
    public ResponseEntity<Map<String, Object>> uploadLogV2(@RequestParam("file") MultipartFile file) {
        return uploadLog(file, "log_v2.txt");
    }

    /**
     * Upload both log files at once
     *
     * @param fileV1 First log file
     * @param fileV2 Second log file
     * @return Response with upload status
     */
    @PostMapping("/upload/both")
    public ResponseEntity<Map<String, Object>> uploadBothLogs(
            @RequestParam("v1") MultipartFile fileV1,
            @RequestParam("v2") MultipartFile fileV2) {

        Map<String, Object> response = new LinkedHashMap<>();

        // Upload both files
        ResponseEntity<Map<String, Object>> v1Response = uploadLog(fileV1, "log_v1.txt");
        ResponseEntity<Map<String, Object>> v2Response = uploadLog(fileV2, "log_v2.txt");

        // Check if both uploads were successful
        if (v1Response.getStatusCode() == HttpStatus.OK && v2Response.getStatusCode() == HttpStatus.OK) {
            response.put("status", "success");
            response.put("message", "Both log files uploaded successfully");
            assert v1Response.getBody() != null;
            response.put("v1", v1Response.getBody().get("file"));
            assert v2Response.getBody() != null;
            response.put("v2", v2Response.getBody().get("file"));
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Error uploading one or both files");
            response.put("v1_status", v1Response.getStatusCode());
            response.put("v2_status", v2Response.getStatusCode());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Compare the uploaded log files (v1 and v2)
     *
     * @return Response with comparison results including line numbers
     */
    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareLogs() {
        try {
            Path v1Path = Paths.get(UPLOAD_DIR, "log_v1.txt");
            Path v2Path = Paths.get(UPLOAD_DIR, "log_v2.txt");

            // Check if both files exist
            if (!Files.exists(v1Path) || !Files.exists(v2Path)) {
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("status", "error");
                error.put("message", "One or both log files not found. Please upload both v1 and v2 logs first.");
                error.put("v1_exists", Files.exists(v1Path));
                error.put("v2_exists", Files.exists(v2Path));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // Perform comparison
            LogComparator.ComparisonResult result = LogComparator.compareLogs(
                    v1Path.toString(),
                    v2Path.toString()
            );

            // Build response
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "success");
            response.put("message", "Log comparison completed successfully");

            // Add statistics
            Map<String, Object> statistics = new LinkedHashMap<>();
            statistics.put("total_lines_v1", result.totalLinesFirstLog);
            statistics.put("total_lines_v2", result.totalLinesSecondLog);
            statistics.put("common_lines", result.commonLines.size());
            statistics.put("only_in_v1", result.onlyInFirstLog.size());
            statistics.put("only_in_v2", result.onlyInSecondLog.size());
            statistics.put("similarity_percent", calculateSimilarity(result));
            response.put("statistics", statistics);

            // Add detailed results
            Map<String, Object> differences = new LinkedHashMap<>();
            differences.put("only_in_v1", formatLogLines(result.onlyInFirstLog));
            differences.put("only_in_v2", formatLogLines(result.onlyInSecondLog));
            differences.put("common_lines", formatLogLines(result.commonLines));
            response.put("differences", differences);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("status", "error");
            error.put("message", "Error reading log files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get upload status (which files are currently uploaded)
     *
     * @return Response with upload status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getUploadStatus() {
        Map<String, Object> response = new LinkedHashMap<>();

        Path v1Path = Paths.get(UPLOAD_DIR, "log_v1.txt");
        Path v2Path = Paths.get(UPLOAD_DIR, "log_v2.txt");

        boolean v1Exists = Files.exists(v1Path);
        boolean v2Exists = Files.exists(v2Path);

        response.put("v1_uploaded", v1Exists);
        response.put("v2_uploaded", v2Exists);
        response.put("both_uploaded", v1Exists && v2Exists);
        response.put("ready_for_comparison", v1Exists && v2Exists);

        if (v1Exists) {
            try {
                response.put("v1_size_bytes", Files.size(v1Path));
                response.put("v1_lines", Files.readAllLines(v1Path).size());
            } catch (IOException e) {
                response.put("v1_error", e.getMessage());
            }
        }

        if (v2Exists) {
            try {
                response.put("v2_size_bytes", Files.size(v2Path));
                response.put("v2_lines", Files.readAllLines(v2Path).size());
            } catch (IOException e) {
                response.put("v2_error", e.getMessage());
            }
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Delete uploaded log files
     *
     * @return Response with deletion status
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearUploadedLogs() {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            Path v1Path = Paths.get(UPLOAD_DIR, "log_v1.txt");
            Path v2Path = Paths.get(UPLOAD_DIR, "log_v2.txt");

            boolean v1Deleted = false;
            boolean v2Deleted = false;

            if (Files.exists(v1Path)) {
                Files.delete(v1Path);
                v1Deleted = true;
            }

            if (Files.exists(v2Path)) {
                Files.delete(v2Path);
                v2Deleted = true;
            }

            response.put("status", "success");
            response.put("message", "Uploaded log files cleared");
            response.put("v1_deleted", v1Deleted);
            response.put("v2_deleted", v2Deleted);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("status", "error");
            response.put("message", "Error deleting files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Helper method to upload a single log file
     */
    private ResponseEntity<Map<String, Object>> uploadLog(MultipartFile file, String filename) {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("status", "error");
                response.put("message", "File is empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                response.put("status", "error");
                response.put("message", "File size exceeds maximum allowed size of 10 MB");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save file
            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());

            // Get file info
            long fileSize = Files.size(filePath);
            int lineCount = Files.readAllLines(filePath).size();

            response.put("status", "success");
            response.put("message", "File uploaded successfully");
            response.put("file", filename);
            response.put("size_bytes", fileSize);
            response.put("lines", lineCount);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("status", "error");
            response.put("message", "Error uploading file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Helper method to format log lines for response
     */
    private List<Map<String, Object>> formatLogLines(List<LogComparator.LogLine> lines) {
        List<Map<String, Object>> formatted = new ArrayList<>();
        for (LogComparator.LogLine line : lines) {
            Map<String, Object> lineMap = new LinkedHashMap<>();
            lineMap.put("line_number", line.lineNumber);
            lineMap.put("content", line.content);
            formatted.add(lineMap);
        }
        return formatted;
    }

    /**
     * Helper method to calculate similarity percentage
     */
    private double calculateSimilarity(LogComparator.ComparisonResult result) {
        if (result.totalLinesFirstLog == 0) {
            return 0.0;
        }
        return (result.commonLines.size() * 100.0) / result.totalLinesFirstLog;
    }
}

