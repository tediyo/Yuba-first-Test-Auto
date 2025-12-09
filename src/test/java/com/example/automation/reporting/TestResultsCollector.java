package com.example.automation.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class TestResultsCollector {
    
    private static final Map<String, TestResult> testResults = new ConcurrentHashMap<>();
    private static final List<String> testExecutionOrder = new ArrayList<>();
    
    public static class TestResult {
        public String testName;
        public String status;
        public long duration;
        public String category;
        public String details;
        public long timestamp;
        public long responseTime; // Response time in milliseconds
        public long loadTime; // Load time in milliseconds
        
        public TestResult(String testName, String status, long duration, String category, String details) {
            this.testName = testName;
            this.status = status;
            this.duration = duration;
            this.category = category;
            this.details = details;
            this.timestamp = System.currentTimeMillis();
            this.responseTime = 0;
            this.loadTime = duration;
        }
        
        public TestResult(String testName, String status, long duration, String category, String details, long responseTime, long loadTime) {
            this.testName = testName;
            this.status = status;
            this.duration = duration;
            this.category = category;
            this.details = details;
            this.timestamp = System.currentTimeMillis();
            this.responseTime = responseTime;
            this.loadTime = loadTime;
        }
    }
    
    public static void recordTestResult(String testName, String status, long duration, String category, String details) {
        TestResult result = new TestResult(testName, status, duration, category, details);
        testResults.put(testName, result);
        
        if (!testExecutionOrder.contains(testName)) {
            testExecutionOrder.add(testName);
        }
        
        System.out.println("📊 Test result recorded: " + testName + " - " + status + " (" + (duration/1000.0) + "s)");
    }
    
    public static void recordTestResult(String testName, String status, long duration, String category, String details, long responseTime, long loadTime) {
        TestResult result = new TestResult(testName, status, duration, category, details, responseTime, loadTime);
        testResults.put(testName, result);
        
        if (!testExecutionOrder.contains(testName)) {
            testExecutionOrder.add(testName);
        }
        
        System.out.println("📊 Test result recorded: " + testName + " - " + status + " (Response: " + (responseTime/1000.0) + "s, Load: " + (loadTime/1000.0) + "s, Total: " + (duration/1000.0) + "s)");
    }
    
    public static Map<String, TestResult> getAllResults() {
        return new ConcurrentHashMap<>(testResults);
    }
    
    public static List<String> getExecutionOrder() {
        return new ArrayList<>(testExecutionOrder);
    }
    
    public static void clearResults() {
        testResults.clear();
        testExecutionOrder.clear();
    }
    
    public static int getTotalTests() {
        return testResults.size();
    }
    
    public static int getPassedTests() {
        return (int) testResults.values().stream().filter(r -> "PASSED".equals(r.status)).count();
    }
    
    public static int getFailedTests() {
        return (int) testResults.values().stream().filter(r -> "FAILED".equals(r.status)).count();
    }
    
    public static double getTotalDuration() {
        return testResults.values().stream().mapToLong(r -> r.duration).sum() / 1000.0;
    }
    
    public static double getAverageDuration() {
        if (testResults.isEmpty()) return 0.0;
        return getTotalDuration() / testResults.size();
    }
}
