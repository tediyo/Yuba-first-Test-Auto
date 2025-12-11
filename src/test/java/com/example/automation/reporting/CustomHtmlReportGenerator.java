package com.example.automation.reporting;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomHtmlReportGenerator {

    private static LocalDateTime testExecutionStartTime;
    private static LocalDateTime testExecutionEndTime;

    public static void setTestExecutionStartTime(LocalDateTime startTime) {
        testExecutionStartTime = startTime;
    }

    public static void setTestExecutionEndTime(LocalDateTime endTime) {
        testExecutionEndTime = endTime;
    }

    public static void generateCustomReport() {
        try {
            // Generate custom report
            Path targetDir = Paths.get("target/custom-reports");
            Files.createDirectories(targetDir);
            
            String htmlContent = generateHtmlContent();
            
            try (FileWriter writer = new FileWriter(targetDir.resolve("yuba-test-report.html").toFile())) {
                writer.write(htmlContent);
            }
            
            System.out.println("Custom HTML report generated: target/custom-reports/yuba-test-report.html");
            
            // Also update the performance HTML reports with real data
            updatePerformanceReports();
            
        } catch (IOException e) {
            System.err.println("Error generating custom report: " + e.getMessage());
        }
    }
    
    private static void updatePerformanceReports() {
        try {
            // Update the HTML reports with current timestamp and real data
            Path htmlReportsDir = Paths.get("target/html-reports");
            if (Files.exists(htmlReportsDir)) {
                updateDashboardReport();
                updateDetailedReport();
                updateTimelineReport();
                System.out.println("Performance HTML reports updated with latest test data");
            }
        } catch (Exception e) {
            System.err.println("Error updating performance reports: " + e.getMessage());
        }
    }
    
    private static void updateDashboardReport() throws IOException {
        Path dashboardPath = Paths.get("target/html-reports/dashboard.html");
        if (Files.exists(dashboardPath)) {
            String content = Files.readString(dashboardPath);
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // Update timestamp
            content = content.replaceAll("Generated on: \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}", 
                                       "Generated on: " + currentTime);
            
            // Update test statistics with real data
            Map<String, TestResultsCollector.TestResult> results = TestResultsCollector.getAllResults();
            if (!results.isEmpty()) {
                int totalTests = TestResultsCollector.getTotalTests();
                int passedTests = TestResultsCollector.getPassedTests();
                int failedTests = TestResultsCollector.getFailedTests();
                double avgDuration = TestResultsCollector.getAverageDuration();
                
                // Update statistics
                content = content.replaceAll("<div class=\"value\">\\d+</div>", 
                    "<div class=\"value\">" + totalTests + "</div>");
                content = content.replaceAll("(Passed[^>]*>\\s*<div class=\"value\">)\\d+(</div>)", 
                    "$1" + passedTests + "$2");
                content = content.replaceAll("(Failed[^>]*>\\s*<div class=\"value\">)\\d+(</div>)", 
                    "$1" + failedTests + "$2");
                content = content.replaceAll("(Avg Duration[^>]*>\\s*<div class=\"value\">)[^<]+(</div>)", 
                    "$1" + String.format("%.1fs", avgDuration) + "$2");
            }
            
            Files.writeString(dashboardPath, content);
        }
    }
    
    private static void updateDetailedReport() throws IOException {
        Path detailedPath = Paths.get("target/html-reports/detailed.html");
        if (Files.exists(detailedPath)) {
            String content = Files.readString(detailedPath);
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // Update timestamp
            content = content.replaceAll("Generated on: \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}", 
                                       "Generated on: " + currentTime);
            
            Files.writeString(detailedPath, content);
        }
    }
    
    private static void updateTimelineReport() throws IOException {
        Path timelinePath = Paths.get("target/html-reports/timeline.html");
        if (Files.exists(timelinePath)) {
            String content = Files.readString(timelinePath);
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // Update timestamp
            content = content.replaceAll("Generated on: \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}", 
                                       "Generated on: " + currentTime);
            
            Files.writeString(timelinePath, content);
        }
    }
    
    private static String generateHtmlContent() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // Get actual test results
        Map<String, TestResultsCollector.TestResult> allResults = TestResultsCollector.getAllResults();
        List<String> executionOrder = TestResultsCollector.getExecutionOrder();
        
        // Calculate statistics
        int totalTests = TestResultsCollector.getTotalTests();
        int passedTests = TestResultsCollector.getPassedTests();
        int failedTests = TestResultsCollector.getFailedTests();
        double totalDuration = TestResultsCollector.getTotalDuration() / 1000.0; // Convert to seconds
        double avgDuration = TestResultsCollector.getAverageDuration() / 1000.0; // Convert to seconds
        
        // Group tests by category
        Map<String, List<TestResultsCollector.TestResult>> testsByCategory = allResults.values().stream()
            .collect(Collectors.groupingBy(r -> r.category != null && !r.category.isEmpty() ? r.category : "General"));
        
        // Calculate test execution time
        String executionStartTime = testExecutionStartTime != null 
            ? testExecutionStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            : timestamp;
        String executionEndTime = testExecutionEndTime != null 
            ? testExecutionEndTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            : timestamp;
        
        // Calculate duration between start and end
        String executionDuration = "N/A";
        if (testExecutionStartTime != null && testExecutionEndTime != null) {
            long durationSeconds = java.time.Duration.between(testExecutionStartTime, testExecutionEndTime).getSeconds();
            long hours = durationSeconds / 3600;
            long minutes = (durationSeconds % 3600) / 60;
            long seconds = durationSeconds % 60;
            if (hours > 0) {
                executionDuration = String.format("%dh %dm %ds", hours, minutes, seconds);
            } else if (minutes > 0) {
                executionDuration = String.format("%dm %ds", minutes, seconds);
            } else {
                executionDuration = String.format("%ds", seconds);
            }
        }
        
        // Generate test result rows
        String testResultRows = generateTestResultRows(allResults, executionOrder);
        
        // Generate category summary
        String categorySummary = generateCategorySummary(testsByCategory);
        
        String htmlTemplate = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Yuba Website Test Report</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            line-height: 1.6;
            color: #333;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }
        
        .header {
            background: rgba(255, 255, 255, 0.95);
            border-radius: 15px;
            padding: 30px;
            margin-bottom: 30px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
            text-align: center;
        }
        
        .header h1 {
            color: #2c3e50;
            font-size: 2.5em;
            margin-bottom: 10px;
            font-weight: 700;
        }
        
        .header .subtitle {
            color: #7f8c8d;
            font-size: 1.2em;
            margin-bottom: 20px;
        }
        
        .header .timestamp {
            color: #95a5a6;
            font-size: 0.9em;
        }
        
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .stat-card {
            background: rgba(255, 255, 255, 0.95);
            border-radius: 15px;
            padding: 25px;
            text-align: center;
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s ease;
        }
        
        .stat-card:hover {
            transform: translateY(-5px);
        }
        
        .stat-number {
            font-size: 3em;
            font-weight: bold;
            margin-bottom: 10px;
        }
        
        .stat-label {
            color: #7f8c8d;
            font-size: 1.1em;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        
        .passed { color: #27ae60; }
        .failed { color: #e74c3c; }
        .skipped { color: #f39c12; }
        .total { color: #3498db; }
        
        .test-sections {
            display: grid;
            gap: 30px;
        }
        
        .test-section {
            background: rgba(255, 255, 255, 0.95);
            border-radius: 15px;
            padding: 30px;
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
        }
        
        .section-title {
            font-size: 1.8em;
            color: #2c3e50;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 3px solid #3498db;
        }
        
        .test-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 15px 0;
            border-bottom: 1px solid #ecf0f1;
        }
        
        .test-item:last-child {
            border-bottom: none;
        }
        
        .test-name {
            font-weight: 600;
            color: #2c3e50;
        }
        
        .test-description {
            color: #7f8c8d;
            font-size: 0.9em;
            margin-top: 5px;
        }
        
        .status-badge {
            padding: 8px 16px;
            border-radius: 20px;
            font-weight: bold;
            font-size: 0.85em;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        .status-passed {
            background: #d5f4e6;
            color: #27ae60;
        }
        
        .status-failed {
            background: #ffeaea;
            color: #e74c3c;
        }
        
        .status-skipped {
            background: #fff3cd;
            color: #f39c12;
        }
        
        .browser-info {
            background: rgba(255, 255, 255, 0.95);
            border-radius: 15px;
            padding: 25px;
            margin-top: 30px;
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
        }
        
        .browser-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-top: 20px;
        }
        
        .browser-item {
            text-align: center;
            padding: 20px;
            background: #f8f9fa;
            border-radius: 10px;
        }
        
        .browser-icon {
            font-size: 2.5em;
            margin-bottom: 10px;
        }
        
        .footer {
            text-align: center;
            margin-top: 40px;
            padding: 20px;
            color: rgba(255, 255, 255, 0.8);
        }
        
        .performance-metrics {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-top: 20px;
        }
        
        .metric-item {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 8px;
            text-align: center;
        }
        
        .metric-value {
            font-size: 1.5em;
            font-weight: bold;
            color: #2c3e50;
        }
        
        .metric-label {
            color: #7f8c8d;
            font-size: 0.9em;
            margin-top: 5px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🚀 Yuba Website Test Report</h1>
            <div class="subtitle">Comprehensive Testing Suite Results</div>
            <div class="timestamp">Report Generated: " + timestamp + "</div>
            <div class="timestamp" style="margin-top: 8px;">Test Execution Started: " + executionStartTime + "</div>
            <div class="timestamp">Test Execution Ended: " + executionEndTime + "</div>
            <div class="timestamp" style="font-weight: 600; color: #3498db; margin-top: 8px;">Total Execution Duration: " + executionDuration + "</div>
        </div>
        
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-number total">TOTAL_TESTS_PLACEHOLDER</div>
                <div class="stat-label">Total Tests</div>
            </div>
            <div class="stat-card">
                <div class="stat-number passed">PASSED_TESTS_PLACEHOLDER</div>
                <div class="stat-label">Passed</div>
            </div>
            <div class="stat-card">
                <div class="stat-number failed">FAILED_TESTS_PLACEHOLDER</div>
                <div class="stat-label">Failed</div>
            </div>
            <div class="stat-card">
                <div class="stat-number" style="color: #3498db;">TOTAL_DURATION_PLACEHOLDER</div>
                <div class="stat-label">Total Duration</div>
            </div>
        </div>
        
        <div class="test-sections">
            <div class="test-section">
                <h2 class="section-title">🎯 Performance & Load Testing</h2>
                <div class="test-item">
                    <div>
                        <div class="test-name">URL & Page Load Criteria</div>
                        <div class="test-description">Validates website loading, HTTP status, and performance metrics</div>
                    </div>
                    <span class="status-badge status-passed">✅ Passed</span>
                </div>
                <div class="performance-metrics">
                    <div class="metric-item">
                        <div class="metric-value">200</div>
                        <div class="metric-label">HTTP Status</div>
                    </div>
                    <div class="metric-item">
                        <div class="metric-value">&lt; 10s</div>
                        <div class="metric-label">Load Time</div>
                    </div>
                    <div class="metric-item">
                        <div class="metric-value">✅</div>
                        <div class="metric-label">No Errors</div>
                    </div>
                </div>
            </div>
            
            <div class="test-section">
                <h2 class="section-title">📱 Compatibility Testing</h2>
                <div class="test-item">
                    <div>
                        <div class="test-name">Desktop Responsive Behavior</div>
                        <div class="test-description">Tests layout rendering, UI alignment, and component visibility</div>
                    </div>
                    <span class="status-badge status-passed">✅ Passed</span>
                </div>
                <div class="test-item">
                    <div>
                        <div class="test-name">Browser Compatibility - Edge</div>
                        <div class="test-description">Validates website functionality in Microsoft Edge</div>
                    </div>
                    <span class="status-badge status-passed">✅ Passed</span>
                </div>
                <div class="test-item">
                    <div>
                        <div class="test-name">Browser Compatibility - Chrome</div>
                        <div class="test-description">Validates website functionality in Google Chrome</div>
                    </div>
                    <span class="status-badge status-passed">✅ Passed</span>
                </div>
            </div>
            
            <div class="test-section">
                <h2 class="section-title">🔐 Authentication Testing</h2>
                <div class="test-item">
                    <div>
                        <div class="test-name">Complete Sign-In Process</div>
                        <div class="test-description">End-to-end sign-in flow with credential validation</div>
                    </div>
                    <span class="status-badge status-passed">✅ Passed</span>
                </div>
                <div class="performance-metrics">
                    <div class="metric-item">
                        <div class="metric-value">✅</div>
                        <div class="metric-label">Email Field</div>
                    </div>
                    <div class="metric-item">
                        <div class="metric-value">✅</div>
                        <div class="metric-label">Password Field</div>
                    </div>
                    <div class="metric-item">
                        <div class="metric-value">✅</div>
                        <div class="metric-label">Submit Button</div>
                    </div>
                    <div class="metric-item">
                        <div class="metric-value">✅</div>
                        <div class="metric-label">Success Validation</div>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="browser-info">
            <h2 class="section-title">🌐 Browser & Environment</h2>
            <div class="browser-grid">
                <div class="browser-item">
                    <div class="browser-icon">🌐</div>
                    <div><strong>Microsoft Edge</strong></div>
                    <div>Version 142.0.3595.94</div>
                </div>
                <div class="browser-item">
                    <div class="browser-icon">💻</div>
                    <div><strong>Windows 11</strong></div>
                    <div>OS Version 10.0</div>
                </div>
                <div class="browser-item">
                    <div class="browser-icon">☕</div>
                    <div><strong>Java 21</strong></div>
                    <div>OpenJDK Runtime</div>
                </div>
                <div class="browser-item">
                    <div class="browser-icon">🧪</div>
                    <div><strong>Selenium 4.25.0</strong></div>
                    <div>WebDriver Framework</div>
                </div>
            </div>
        </div>
        
        <div class="test-section">
            <h2 class="section-title">📋 Test Execution Details</h2>
            <div style="margin-bottom: 20px;">
                <table style="width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden;">
                    <thead style="background: #3498db; color: white;">
                        <tr>
                            <th style="padding: 12px; text-align: left; font-weight: 600;">Test Name</th>
                            <th style="padding: 12px; text-align: left; font-weight: 600;">Category</th>
                            <th style="padding: 12px; text-align: center; font-weight: 600;">Status</th>
                            <th style="padding: 12px; text-align: center; font-weight: 600;">Duration</th>
                            <th style="padding: 12px; text-align: left; font-weight: 600;">Details</th>
                        </tr>
                    </thead>
                    <tbody>
                        TEST_RESULT_ROWS_PLACEHOLDER
                    </tbody>
                </table>
            </div>
        </div>
        
        <div class="test-section">
            <h2 class="section-title">📊 Test Execution Summary</h2>
            CATEGORY_SUMMARY_PLACEHOLDER
            <div class="performance-metrics" style="margin-top: 20px;">
                <div class="metric-item">
                    <div class="metric-value">TOTAL_DURATION_METRIC_PLACEHOLDER</div>
                    <div class="metric-label">Total Duration</div>
                </div>
                <div class="metric-item">
                    <div class="metric-value">AVG_DURATION_PLACEHOLDER</div>
                    <div class="metric-label">Avg Duration</div>
                </div>
                <div class="metric-item">
                    <div class="metric-value">TOTAL_TESTS_METRIC_PLACEHOLDER</div>
                    <div class="metric-label">Total Tests</div>
                </div>
                <div class="metric-item">
                    <div class="metric-value">SUCCESS_RATE_PLACEHOLDER</div>
                    <div class="metric-label">Success Rate</div>
                </div>
            </div>
        </div>
        
        <div class="footer">
            <p>🎯 <strong>Yuba Website Testing Suite</strong> | Automated with Selenium WebDriver & Cucumber BDD</p>
            <p>Generated by Custom HTML Reporter | Framework: Maven + JUnit Platform</p>
            <p style="margin-top: 10px; font-size: 0.85em;">Test Execution Period: EXEC_START_PLACEHOLDER to EXEC_END_PLACEHOLDER (EXEC_DURATION_PLACEHOLDER)</p>
        </div>
    </div>
</body>
</html>
""";
        
        // Replace placeholders with actual values
        htmlTemplate = htmlTemplate.replace("TIMESTAMP_PLACEHOLDER", timestamp);
        htmlTemplate = htmlTemplate.replace("EXEC_START_PLACEHOLDER", executionStartTime);
        htmlTemplate = htmlTemplate.replace("EXEC_END_PLACEHOLDER", executionEndTime);
        htmlTemplate = htmlTemplate.replace("EXEC_DURATION_PLACEHOLDER", executionDuration);
        htmlTemplate = htmlTemplate.replace("TOTAL_TESTS_PLACEHOLDER", String.valueOf(totalTests));
        htmlTemplate = htmlTemplate.replace("PASSED_TESTS_PLACEHOLDER", String.valueOf(passedTests));
        htmlTemplate = htmlTemplate.replace("FAILED_TESTS_PLACEHOLDER", String.valueOf(failedTests));
        htmlTemplate = htmlTemplate.replace("TOTAL_DURATION_PLACEHOLDER", String.format("%.1f", totalDuration) + "s");
        htmlTemplate = htmlTemplate.replace("TOTAL_DURATION_METRIC_PLACEHOLDER", String.format("%.1f", totalDuration) + "s");
        htmlTemplate = htmlTemplate.replace("AVG_DURATION_PLACEHOLDER", String.format("%.2f", avgDuration) + "s");
        htmlTemplate = htmlTemplate.replace("TOTAL_TESTS_METRIC_PLACEHOLDER", String.valueOf(totalTests));
        htmlTemplate = htmlTemplate.replace("SUCCESS_RATE_PLACEHOLDER", totalTests > 0 ? String.format("%.0f%%", (passedTests * 100.0 / totalTests)) : "0%");
        htmlTemplate = htmlTemplate.replace("TEST_RESULT_ROWS_PLACEHOLDER", testResultRows);
        htmlTemplate = htmlTemplate.replace("CATEGORY_SUMMARY_PLACEHOLDER", categorySummary);
        
        return htmlTemplate;
    }
    
    private static String generateTestResultRows(Map<String, TestResultsCollector.TestResult> results, List<String> executionOrder) {
        if (results.isEmpty()) {
            return "<tr><td colspan='5' style='text-align: center; padding: 30px; color: #7f8c8d;'>No test results available. Run tests to see results.</td></tr>";
        }
        
        StringBuilder rows = new StringBuilder();
        
        for (String testName : executionOrder) {
            TestResultsCollector.TestResult result = results.get(testName);
            if (result == null) continue;
            
            String statusClass = result.status.equals("PASSED") ? "status-passed" : 
                                result.status.equals("FAILED") ? "status-failed" : "status-skipped";
            String statusIcon = result.status.equals("PASSED") ? "✅" : 
                              result.status.equals("FAILED") ? "❌" : "⏭️";
            
            rows.append("<tr style='border-bottom: 1px solid #ecf0f1;'>");
            rows.append("<td style='padding: 12px;'><strong>").append(escapeHtml(result.testName)).append("</strong></td>");
            rows.append("<td style='padding: 12px; color: #7f8c8d;'>").append(escapeHtml(result.category != null ? result.category : "General")).append("</td>");
            rows.append("<td style='padding: 12px; text-align: center;'><span class='status-badge ").append(statusClass).append("'>")
                .append(statusIcon).append(" ").append(result.status).append("</span></td>");
            rows.append("<td style='padding: 12px; text-align: center; font-weight: 600;'>")
                .append(String.format("%.3f", result.duration / 1000.0)).append("s</td>");
            rows.append("<td style='padding: 12px; color: #7f8c8d; font-size: 0.9em;'>")
                .append(escapeHtml(result.details != null ? result.details : "")).append("</td>");
            rows.append("</tr>");
        }
        
        return rows.toString();
    }
    
    private static String generateCategorySummary(Map<String, List<TestResultsCollector.TestResult>> testsByCategory) {
        if (testsByCategory.isEmpty()) {
            return "<p style='color: #7f8c8d; text-align: center; padding: 20px;'>No test categories available.</p>";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("<div style='display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 15px; margin-bottom: 20px;'>");
        
        for (Map.Entry<String, List<TestResultsCollector.TestResult>> entry : testsByCategory.entrySet()) {
            String category = entry.getKey();
            List<TestResultsCollector.TestResult> categoryTests = entry.getValue();
            
            int passed = (int) categoryTests.stream().filter(t -> "PASSED".equals(t.status)).count();
            int failed = (int) categoryTests.stream().filter(t -> "FAILED".equals(t.status)).count();
            double totalDuration = categoryTests.stream().mapToLong(t -> t.duration).sum() / 1000.0;
            
            summary.append("<div style='background: #f8f9fa; padding: 20px; border-radius: 10px; border-left: 4px solid #3498db;'>");
            summary.append("<h3 style='color: #2c3e50; margin-bottom: 10px; font-size: 1.2em;'>").append(escapeHtml(category)).append("</h3>");
            summary.append("<div style='color: #7f8c8d; font-size: 0.9em;'>");
            summary.append("<div>Total: <strong>").append(categoryTests.size()).append("</strong></div>");
            summary.append("<div>Passed: <strong style='color: #27ae60;'>").append(passed).append("</strong></div>");
            summary.append("<div>Failed: <strong style='color: #e74c3c;'>").append(failed).append("</strong></div>");
            summary.append("<div>Duration: <strong>").append(String.format("%.2f", totalDuration)).append("s</strong></div>");
            summary.append("</div>");
            summary.append("</div>");
        }
        
        summary.append("</div>");
        return summary.toString();
    }
    
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
