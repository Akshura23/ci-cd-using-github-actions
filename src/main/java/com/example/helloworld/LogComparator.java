package com.example.helloworld;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * LogComparator - A utility class to compare two log files and identify differences
 * with line numbers and content.
 */
public class LogComparator {

    /**
     * Represents a log line with its line number and content
     */
    public static class LogLine {
        public final int lineNumber;
        public final String content;

        public LogLine(int lineNumber, String content) {
            this.lineNumber = lineNumber;
            this.content = content;
        }

        @Override
        public String toString() {
            return String.format("[Line %d] %s", lineNumber, content);
        }
    }

    /**
     * Result set containing differences between two log files
     */
    public static class ComparisonResult {
        public final List<LogLine> onlyInFirstLog;
        public final List<LogLine> onlyInSecondLog;
        public final List<LogLine> commonLines;
        public final int totalLinesFirstLog;
        public final int totalLinesSecondLog;

        public ComparisonResult(List<LogLine> onlyInFirst, List<LogLine> onlyInSecond,
                                List<LogLine> common, int totalFirst, int totalSecond) {
            this.onlyInFirstLog = onlyInFirst;
            this.onlyInSecondLog = onlyInSecond;
            this.commonLines = common;
            this.totalLinesFirstLog = totalFirst;
            this.totalLinesSecondLog = totalSecond;
        }

        public void printSummary() {
            System.out.println("\n========== LOG COMPARISON RESULT ==========");
            System.out.println("First Log Total Lines: " + totalLinesFirstLog);
            System.out.println("Second Log Total Lines: " + totalLinesSecondLog);
            System.out.println("Common Lines: " + commonLines.size());
            System.out.println("Only in First Log: " + onlyInFirstLog.size());
            System.out.println("Only in Second Log: " + onlyInSecondLog.size());
            System.out.println("==========================================\n");
        }

        public void printDetails() {
            printSummary();

            if (!onlyInFirstLog.isEmpty()) {
                System.out.println("--- Lines only in First Log ---");
                onlyInFirstLog.forEach(System.out::println);
                System.out.println();
            }

            if (!onlyInSecondLog.isEmpty()) {
                System.out.println("--- Lines only in Second Log ---");
                onlyInSecondLog.forEach(System.out::println);
                System.out.println();
            }

            if (!commonLines.isEmpty()) {
                System.out.println("--- Common Lines (" + commonLines.size() + " lines) ---");
                commonLines.forEach(System.out::println);
                System.out.println();
            }
        }
    }

    /**
     * Reads a log file and returns a list of LogLine objects
     *
     * @param filePath Path to the log file
     * @return List of LogLine objects
     * @throws IOException if file cannot be read
     */
    public static List<LogLine> readLogFile(String filePath) throws IOException {
        List<LogLine> lines = new ArrayList<>();
        List<String> fileLines = Files.readAllLines(Paths.get(filePath));

        for (int i = 0; i < fileLines.size(); i++) {
            lines.add(new LogLine(i + 1, fileLines.get(i)));
        }

        return lines;
    }

    /**
     * Compares two log files and returns differences
     *
     * @param firstLogPath Path to first log file
     * @param secondLogPath Path to second log file
     * @return ComparisonResult containing differences with line numbers
     * @throws IOException if files cannot be read
     */
    public static ComparisonResult compareLogs(String firstLogPath, String secondLogPath)
            throws IOException {
        List<LogLine> firstLog = readLogFile(firstLogPath);
        List<LogLine> secondLog = readLogFile(secondLogPath);

        return compareLogs(firstLog, secondLog);
    }

    /**
     * Compares two lists of log lines
     *
     * @param firstLog First list of log lines
     * @param secondLog Second list of log lines
     * @return ComparisonResult containing differences with line numbers
     */
    public static ComparisonResult compareLogs(List<LogLine> firstLog, List<LogLine> secondLog) {
        Set<String> secondLogContents = new HashSet<>();
        for (LogLine line : secondLog) {
            secondLogContents.add(line.content);
        }

        Set<String> firstLogContents = new HashSet<>();
        for (LogLine line : firstLog) {
            firstLogContents.add(line.content);
        }

        List<LogLine> onlyInFirst = new ArrayList<>();
        List<LogLine> onlyInSecond = new ArrayList<>();
        List<LogLine> common = new ArrayList<>();

        // Find lines only in first log
        for (LogLine line : firstLog) {
            if (!secondLogContents.contains(line.content)) {
                onlyInFirst.add(line);
            } else {
                common.add(line);
            }
        }

        // Find lines only in second log
        for (LogLine line : secondLog) {
            if (!firstLogContents.contains(line.content)) {
                onlyInSecond.add(line);
            }
        }

        return new ComparisonResult(onlyInFirst, onlyInSecond, common,
                firstLog.size(), secondLog.size());
    }


}

