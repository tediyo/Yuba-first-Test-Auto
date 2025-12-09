package com.example.automation.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PerformanceTracker {
    
    private static final Map<String, PerformanceMetric> stepMetrics = new ConcurrentHashMap<>();
    private static final List<String> stepExecutionOrder = new ArrayList<>();
    
    public static class PerformanceMetric {
        public String stepName;
        public String stepDescription;
        public long responseTime; // Time from action start to response received
        public long loadTime; // Time for page/content to fully load
        public long totalTime; // Total time (responseTime + loadTime)
        public long timestamp;
        public String actionType; // "navigation", "click", "input", "submit", etc.
        public String status; // "PASSED", "FAILED", "SKIPPED"
        
        public PerformanceMetric(String stepName, String stepDescription, String actionType) {
            this.stepName = stepName;
            this.stepDescription = stepDescription;
            this.actionType = actionType;
            this.timestamp = System.currentTimeMillis();
            this.status = "PASSED";
        }
    }
    
    /**
     * Start tracking a step's performance
     */
    public static String startStep(String stepName, String stepDescription, String actionType) {
        String stepId = stepName + "_" + System.currentTimeMillis();
        PerformanceMetric metric = new PerformanceMetric(stepName, stepDescription, actionType);
        metric.responseTime = System.currentTimeMillis();
        stepMetrics.put(stepId, metric);
        
        if (!stepExecutionOrder.contains(stepId)) {
            stepExecutionOrder.add(stepId);
        }
        
        return stepId;
    }
    
    /**
     * Record response time (when response is received)
     */
    public static void recordResponseTime(String stepId, long responseStartTime) {
        PerformanceMetric metric = stepMetrics.get(stepId);
        if (metric != null) {
            metric.responseTime = System.currentTimeMillis() - responseStartTime;
        }
    }
    
    /**
     * Complete step tracking with load time
     */
    public static void completeStep(String stepId, long actionStartTime, long loadEndTime) {
        PerformanceMetric metric = stepMetrics.get(stepId);
        if (metric != null) {
            long totalDuration = loadEndTime - actionStartTime;
            metric.loadTime = totalDuration - metric.responseTime;
            metric.totalTime = totalDuration;
            metric.status = "PASSED";
        }
    }
    
    /**
     * Complete step tracking with separate response and load times
     */
    public static void completeStep(String stepId, long actionStartTime, long responseTime, long loadEndTime) {
        PerformanceMetric metric = stepMetrics.get(stepId);
        if (metric != null) {
            metric.responseTime = responseTime - actionStartTime;
            metric.loadTime = loadEndTime - responseTime;
            metric.totalTime = loadEndTime - actionStartTime;
            metric.status = "PASSED";
        }
    }
    
    /**
     * Mark step as failed
     */
    public static void failStep(String stepId, long actionStartTime) {
        PerformanceMetric metric = stepMetrics.get(stepId);
        if (metric != null) {
            metric.totalTime = System.currentTimeMillis() - actionStartTime;
            metric.status = "FAILED";
        }
    }
    
    /**
     * Get all step metrics
     */
    public static Map<String, PerformanceMetric> getAllMetrics() {
        return new ConcurrentHashMap<>(stepMetrics);
    }
    
    /**
     * Get step execution order
     */
    public static List<String> getExecutionOrder() {
        return new ArrayList<>(stepExecutionOrder);
    }
    
    /**
     * Get metrics for a specific step by name
     */
    public static List<PerformanceMetric> getMetricsByStepName(String stepName) {
        List<PerformanceMetric> results = new ArrayList<>();
        for (PerformanceMetric metric : stepMetrics.values()) {
            if (metric.stepName.equals(stepName)) {
                results.add(metric);
            }
        }
        return results;
    }
    
    /**
     * Calculate total execution time
     */
    public static long getTotalExecutionTime() {
        if (stepMetrics.isEmpty()) return 0;
        
        long firstTimestamp = stepMetrics.values().stream()
            .mapToLong(m -> m.timestamp)
            .min()
            .orElse(0);
        
        long lastTimestamp = stepMetrics.values().stream()
            .mapToLong(m -> m.timestamp + m.totalTime)
            .max()
            .orElse(0);
        
        return lastTimestamp - firstTimestamp;
    }
    
    /**
     * Get average response time
     */
    public static double getAverageResponseTime() {
        if (stepMetrics.isEmpty()) return 0.0;
        return stepMetrics.values().stream()
            .mapToLong(m -> m.responseTime)
            .average()
            .orElse(0.0) / 1000.0;
    }
    
    /**
     * Get average load time
     */
    public static double getAverageLoadTime() {
        if (stepMetrics.isEmpty()) return 0.0;
        return stepMetrics.values().stream()
            .mapToLong(m -> m.loadTime)
            .average()
            .orElse(0.0) / 1000.0;
    }
    
    /**
     * Clear all metrics
     */
    public static void clearMetrics() {
        stepMetrics.clear();
        stepExecutionOrder.clear();
    }
}

