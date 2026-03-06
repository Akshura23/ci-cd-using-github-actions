package com.example.helloworld;

import java.io.IOException;
import java.util.List;

/**
 * Example usage scenarios for LogComparator
 */
public class LogComparatorExamples {

    /**
     * Example 1: Simple comparison with summary
     */
    public static void example1_SimpleComparison() throws IOException {
        System.out.println("=== Example 1: Simple Comparison ===\n");

        LogComparator.ComparisonResult result =
            LogComparator.compareLogs("log1.txt", "log2.txt");

        result.printSummary();
    }

    /**
     * Example 2: Detailed analysis
     */
    public static void example2_DetailedAnalysis() throws IOException {
        System.out.println("=== Example 2: Detailed Analysis ===\n");

        LogComparator.ComparisonResult result =
            LogComparator.compareLogs("log1.txt", "log2.txt");

        result.printDetails();
    }

    /**
     * Example 3: Process only differences
     */
    public static void example3_ProcessDifferences() throws IOException {
        System.out.println("=== Example 3: Process Only Differences ===\n");

        LogComparator.ComparisonResult result =
            LogComparator.compareLogs("log1.txt", "log2.txt");

        System.out.println("Lines that disappeared from first to second:");
        for (LogComparator.LogLine line : result.onlyInFirstLog) {
            System.out.println("  " + line);
        }

        System.out.println("\nLines that were added from first to second:");
        for (LogComparator.LogLine line : result.onlyInSecondLog) {
            System.out.println("  " + line);
        }
    }

    /**
     * Example 4: Filter and process specific differences
     */
    public static void example4_FilterDifferences() throws IOException {
        System.out.println("=== Example 4: Filter Specific Differences ===\n");

        LogComparator.ComparisonResult result =
            LogComparator.compareLogs("log1.txt", "log2.txt");

        // Find ERROR entries only in first log
        System.out.println("ERROR entries only in first log:");
        for (LogComparator.LogLine line : result.onlyInFirstLog) {
            if (line.content.contains("ERROR")) {
                System.out.println("  " + line);
            }
        }

        // Find INFO entries only in second log
        System.out.println("\nINFO entries only in second log:");
        for (LogComparator.LogLine line : result.onlyInSecondLog) {
            if (line.content.contains("INFO")) {
                System.out.println("  " + line);
            }
        }
    }

    /**
     * Example 5: Statistics and reporting
     */
    public static void example5_Statistics() throws IOException {
        System.out.println("=== Example 5: Statistics and Reporting ===\n");

        LogComparator.ComparisonResult result =
            LogComparator.compareLogs("log1.txt", "log2.txt");

        int totalDifferences = result.onlyInFirstLog.size() + result.onlyInSecondLog.size();
        double similarityPercent = (result.commonLines.size() * 100.0) /
                                   result.totalLinesFirstLog;

        System.out.printf("Total Differences: %d%n", totalDifferences);
        System.out.printf("Similarity: %.2f%%%n", similarityPercent);
        System.out.printf("Removed Lines: %d%n", result.onlyInFirstLog.size());
        System.out.printf("Added Lines: %d%n", result.onlyInSecondLog.size());
        System.out.printf("Common Lines: %d%n", result.commonLines.size());
    }

    /**
     * Example 6: Compare log lists directly (programmatic)
     */
    public static void example6_ProgrammaticComparison() throws IOException {
        System.out.println("=== Example 6: Programmatic Comparison ===\n");

        // Read logs programmatically
        List<LogComparator.LogLine> firstLog = LogComparator.readLogFile("log1.txt");
        List<LogComparator.LogLine> secondLog = LogComparator.readLogFile("log2.txt");

        // Compare using lists
        LogComparator.ComparisonResult result =
            LogComparator.compareLogs(firstLog, secondLog);

        System.out.println("First log has " + firstLog.size() + " lines");
        System.out.println("Second log has " + secondLog.size() + " lines");
        System.out.println("Comparison complete: " +
                          result.commonLines.size() + " lines match");
    }

    /**
     * Example 7: Find specific log level differences
     */
    public static void example7_LogLevelAnalysis() throws IOException {
        System.out.println("=== Example 7: Log Level Analysis ===\n");

        LogComparator.ComparisonResult result =
            LogComparator.compareLogs("log1.txt", "log2.txt");

        countLogLevel(result.onlyInFirstLog, "First Log Only");
        countLogLevel(result.onlyInSecondLog, "Second Log Only");
        countLogLevel(result.commonLines, "Common");
    }

    private static void countLogLevel(List<LogComparator.LogLine> lines, String label) {
        int infoCount = 0;
        int debugCount = 0;
        int errorCount = 0;
        int warnCount = 0;

        for (LogComparator.LogLine line : lines) {
            if (line.content.contains(" INFO ")) infoCount++;
            else if (line.content.contains(" DEBUG ")) debugCount++;
            else if (line.content.contains(" ERROR ")) errorCount++;
            else if (line.content.contains(" WARN ")) warnCount++;
        }

        System.out.printf("%s:%n", label);
        System.out.printf("  INFO: %d, DEBUG: %d, ERROR: %d, WARN: %d%n",
                         infoCount, debugCount, errorCount, warnCount);
    }

    /**
     * Example 8: Export differences to formatted output
     */
    public static void example8_ExportDifferences() throws IOException {
        System.out.println("=== Example 8: Export Differences ===\n");

        LogComparator.ComparisonResult result =
            LogComparator.compareLogs("log1.txt", "log2.txt");

        // Export in CSV-like format
        System.out.println("LINE_NUMBER,TYPE,CONTENT");

        for (LogComparator.LogLine line : result.onlyInFirstLog) {
            System.out.printf("%d,ONLY_IN_FIRST,\"%s\"%n",
                            line.lineNumber,
                            line.content.replace("\"", "\\\""));
        }

        for (LogComparator.LogLine line : result.onlyInSecondLog) {
            System.out.printf("%d,ONLY_IN_SECOND,\"%s\"%n",
                            line.lineNumber,
                            line.content.replace("\"", "\\\""));
        }
    }


}

