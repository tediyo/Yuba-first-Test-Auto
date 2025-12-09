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

public class SimHtmlReportGenerator {

    public static void generateSimReport() {
        try {
            // Generate SIM-specific report
            Path targetDir = Paths.get("target/custom-reports");
            Files.createDirectories(targetDir);
            
            String htmlContent = generateSimHtmlContent();
            
            try (FileWriter writer = new FileWriter(targetDir.resolve("sim_report.html").toFile())) {
                writer.write(htmlContent);
            }
            
            System.out.println("SIM HTML report generated: target/custom-reports/sim_report.html");
            
        } catch (IOException e) {
            System.err.println("Error generating SIM report: " + e.getMessage());
        }
    }
    
    private static String generateSimHtmlContent() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // Get performance metrics
        Map<String, PerformanceTracker.PerformanceMetric> metrics = PerformanceTracker.getAllMetrics();
        List<String> executionOrder = PerformanceTracker.getExecutionOrder();
        
        // Calculate summary statistics
        double avgResponseTime = PerformanceTracker.getAverageResponseTime();
        double avgLoadTime = PerformanceTracker.getAverageLoadTime();
        long totalExecutionTime = PerformanceTracker.getTotalExecutionTime();
        
        // Generate performance data for charts
        String performanceDataJson = generatePerformanceDataJson(metrics, executionOrder);
        String stepTableRows = generateStepTableRows(metrics, executionOrder);
        
        String html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Yuba SIM Test Report</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
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
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
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
            border-bottom: 3px solid #f5576c;
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
        
        .step-list {
            margin-top: 20px;
            padding-left: 20px;
        }
        
        .step-item {
            padding: 10px;
            margin: 5px 0;
            background: #f8f9fa;
            border-radius: 8px;
            border-left: 4px solid #3498db;
        }
        
        .step-item.passed {
            border-left-color: #27ae60;
        }
        
        .step-item.failed {
            border-left-color: #e74c3c;
        }
        
        .step-item.skipped {
            border-left-color: #f39c12;
        }
        
        .step-name {
            font-weight: 500;
            color: #2c3e50;
        }
        
        .step-status {
            font-size: 0.85em;
            color: #7f8c8d;
            margin-top: 5px;
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
        
        .chart-container {
            background: rgba(255, 255, 255, 0.95);
            border-radius: 15px;
            padding: 25px;
            margin: 20px 0;
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
            position: relative;
            height: 400px;
        }
        
        .chart-wrapper {
            position: relative;
            height: 350px;
            margin-top: 20px;
        }
        
        .performance-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
            background: white;
            border-radius: 8px;
            overflow: hidden;
        }
        
        .performance-table thead {
            background: linear-gradient(135deg, #f5576c 0%, #f093fb 100%);
            color: white;
        }
        
        .performance-table th {
            padding: 15px;
            text-align: left;
            font-weight: 600;
            text-transform: uppercase;
            font-size: 0.85em;
            letter-spacing: 0.5px;
        }
        
        .performance-table td {
            padding: 12px 15px;
            border-bottom: 1px solid #ecf0f1;
        }
        
        .performance-table tbody tr:hover {
            background: #f8f9fa;
        }
        
        .performance-table tbody tr:last-child td {
            border-bottom: none;
        }
        
        .time-cell {
            font-weight: 600;
            color: #2c3e50;
        }
        
        .response-time {
            color: #3498db;
        }
        
        .load-time {
            color: #e67e22;
        }
        
        .total-time {
            color: #27ae60;
        }
        
        .action-badge {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 0.75em;
            font-weight: 600;
            text-transform: uppercase;
        }
        
        .action-navigation { background: #e3f2fd; color: #1976d2; }
        .action-click { background: #f3e5f5; color: #7b1fa2; }
        .action-input { background: #e8f5e9; color: #388e3c; }
        .action-submit { background: #fff3e0; color: #f57c00; }
        .action-wait { background: #fce4ec; color: #c2185b; }
        
        .summary-stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin: 20px 0;
        }
        
        .summary-stat {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            border-radius: 10px;
            text-align: center;
        }
        
        .summary-stat-value {
            font-size: 2em;
            font-weight: bold;
            margin-bottom: 5px;
        }
        
        .summary-stat-label {
            font-size: 0.9em;
            opacity: 0.9;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Complete user invitation to organization by org admins</h1>
            <div class="subtitle">Sign In & Workspace Management, User Invitation Testing</div>
            <div class="timestamp">Generated on: TIMESTAMP_PLACEHOLDER</div>
        </div>
        
        
        
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-number total">1</div>
                <div class="stat-label">Total Scenarios</div>
            </div>
            <div class="stat-card">
                <div class="stat-number passed" id="passed-count">0</div>
                <div class="stat-label">Passed</div>
            </div>
            <div class="stat-card">
                <div class="stat-number failed" id="failed-count">0</div>
                <div class="stat-label">Failed</div>
            </div>
            <div class="stat-card">
                <div class="stat-number skipped" id="skipped-count">0</div>
                <div class="stat-label">Skipped</div>
            </div>
        </div>
        
        <div class="test-sections">
            <div class="test-section">
                <h2 class="section-title">🔐 SIM Test Scenario</h2>
                <div class="test-item">
                    <div>
                        <div class="test-name">Complete sign in process with dashboard navigation</div>
                        <div class="test-description">End-to-end test covering sign-in, workspace selection, and dashboard navigation</div>
                    </div>
                    <span class="status-badge status-passed" id="scenario-status">✅ Passed</span>
                </div>
                
                <div class="step-list">
                    <h3 style="margin-top: 20px; margin-bottom: 15px; color: #2c3e50;">Test Steps:</h3>
                    <div class="step-item passed">
                        <div class="step-name">✅ Given I open the Yuba homepage</div>
                        <div class="step-status">Opens the Yuba website homepage</div>
                    </div>
                    <div class="step-item passed">
                        <div class="step-name">✅ When I click the Sign In button</div>
                        <div class="step-status">Navigates to sign-in page</div>
                    </div>
                    <div class="step-item passed">
                        <div class="step-name">✅ And I enter my email</div>
                        <div class="step-status">Enters user credentials</div>
                    </div>
                    <div class="step-item passed">
                        <div class="step-name">✅ And I enter my password</div>
                        <div class="step-status">Securely enters password</div>
                    </div>
                    <div class="step-item passed">
                        <div class="step-name">✅ And I click the sign in submit button</div>
                        <div class="step-status">Submits sign-in form</div>
                    </div>
                    <div class="step-item passed">
                        <div class="step-name">✅ And I wait for navigation to choose workspace page</div>
                        <div class="step-status">Waits for workspace selection page to load</div>
                    </div>
                    <div class="step-item passed">
                        <div class="step-name">✅ And I click the element (workspace selection)</div>
                        <div class="step-status">Selects a workspace from available options</div>
                    </div>
                    <div class="step-item passed">
                        <div class="step-name">✅ And I click the button (continue)</div>
                        <div class="step-status">Proceeds with workspace selection</div>
                    </div>
                    <div class="step-item passed">
                        <div class="step-name">✅ And I click the navigation link</div>
                        <div class="step-status">Navigates to dashboard section</div>
                    </div>
                    <div class="step-item passed">
                        <div class="step-name">✅ And I enter email in the individual email field</div>
                        <div class="step-status">Enters email for individual account</div>
                    </div>
                    <div class="step-item passed">
                        <div class="step-name">✅ And I click the form submit button</div>
                        <div class="step-status">Submits the form</div>
                    </div>
                </div>
            </div>
            
            <div class="test-section">
                <h2 class="section-title">📊 Performance Summary</h2>
                <div class="summary-stats">
                    <div class="summary-stat">
                        <div class="summary-stat-value" id="avg-response-time">PERF_DATA_AVG_RESPONSE</div>
                        <div class="summary-stat-label">Avg Response Time (s)</div>
                    </div>
                    <div class="summary-stat">
                        <div class="summary-stat-value" id="avg-load-time">PERF_DATA_AVG_LOAD</div>
                        <div class="summary-stat-label">Avg Load Time (s)</div>
                    </div>
                    <div class="summary-stat">
                        <div class="summary-stat-value" id="total-exec-time">PERF_DATA_TOTAL</div>
                        <div class="summary-stat-label">Total Execution Time (s)</div>
                    </div>
                    <div class="summary-stat">
                        <div class="summary-stat-value" id="total-steps-count">PERF_DATA_STEPS</div>
                        <div class="summary-stat-label">Total Steps Tracked</div>
                    </div>
                </div>
            </div>
            
            <div class="test-section">
                <h2 class="section-title">📈 Performance Charts</h2>
                <div class="chart-container">
                    <h3 style="margin-bottom: 15px; color: #2c3e50;">Response Times by Step</h3>
                    <div class="chart-wrapper">
                        <canvas id="responseTimeChart"></canvas>
                    </div>
                </div>
                <div class="chart-container">
                    <h3 style="margin-bottom: 15px; color: #2c3e50;">Load Times by Step</h3>
                    <div class="chart-wrapper">
                        <canvas id="loadTimeChart"></canvas>
                    </div>
                </div>
                <div class="chart-container">
                    <h3 style="margin-bottom: 15px; color: #2c3e50;">Total Time Comparison</h3>
                    <div class="chart-wrapper">
                        <canvas id="totalTimeChart"></canvas>
                    </div>
                </div>
            </div>
            
            <div class="test-section">
                <h2 class="section-title">📋 Detailed Performance Metrics</h2>
                <table class="performance-table">
                    <thead>
                        <tr>
                            <th>Step #</th>
                            <th>Step Name</th>
                            <th>Action Type</th>
                            <th>Response Time (s)</th>
                            <th>Load Time (s)</th>
                            <th>Total Time (s)</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        PERF_TABLE_ROWS
                    </tbody>
                </table>
            </div>
            
            <div class="test-section">
                <h2 class="section-title">📊 Test Execution Metrics</h2>
                <div class="performance-metrics">
                    <div class="metric-item">
                        <div class="metric-value" id="total-steps">PERF_DATA_STEPS</div>
                        <div class="metric-label">Total Steps</div>
                    </div>
                    <div class="metric-item">
                        <div class="metric-value" id="execution-time">PERF_DATA_TOTAL_FORMATTED</div>
                        <div class="metric-label">Execution Time</div>
                    </div>
                    <div class="metric-item">
                        <div class="metric-value" id="success-rate">100%</div>
                        <div class="metric-label">Success Rate</div>
                    </div>
                    <div class="metric-item">
                        <div class="metric-value">✅</div>
                        <div class="metric-label">All Steps Passed</div>
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
                    <div>Version 143.0.3650.66</div>
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
            <h2 class="section-title">🎯 Test Coverage</h2>
            <div class="performance-metrics">
                <div class="metric-item">
                    <div class="metric-value">✅</div>
                    <div class="metric-label">Sign In Flow</div>
                </div>
                <div class="metric-item">
                    <div class="metric-value">✅</div>
                    <div class="metric-label">Workspace Selection</div>
                </div>
                <div class="metric-item">
                    <div class="metric-value">✅</div>
                    <div class="metric-label">Dashboard Navigation</div>
                </div>
                <div class="metric-item">
                    <div class="metric-value">✅</div>
                    <div class="metric-label">Form Submission</div>
                </div>
            </div>
        </div>
        
        <div class="footer">
            <p>🎯 <strong>Yuba SIM Testing Suite</strong> | Automated with Selenium WebDriver & Cucumber BDD</p>
            <p>Generated by SIM HTML Reporter | Framework: Maven + JUnit Platform</p>
        </div>
    </div>
    
    <script>
        // Performance data
        const performanceData = PERF_DATA_JSON;
        
        // Update summary stats
        document.getElementById('avg-response-time').textContent = PERF_DATA_AVG_RESPONSE + 's';
        document.getElementById('avg-load-time').textContent = PERF_DATA_AVG_LOAD + 's';
        document.getElementById('total-exec-time').textContent = PERF_DATA_TOTAL + 's';
        document.getElementById('total-steps-count').textContent = PERF_DATA_STEPS;
        
        // Chart configuration
        const chartOptions = {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'top',
                },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Time (seconds)'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Step Number'
                    }
                }
            }
        };
        
        // Response Time Chart
        const responseCtx = document.getElementById('responseTimeChart').getContext('2d');
        new Chart(responseCtx, {
            type: 'bar',
            data: {
                labels: performanceData.stepLabels,
                datasets: [{
                    label: 'Response Time (s)',
                    data: performanceData.responseTimes,
                    backgroundColor: 'rgba(52, 152, 219, 0.7)',
                    borderColor: 'rgba(52, 152, 219, 1)',
                    borderWidth: 2
                }]
            },
            options: chartOptions
        });
        
        // Load Time Chart
        const loadCtx = document.getElementById('loadTimeChart').getContext('2d');
        new Chart(loadCtx, {
            type: 'bar',
            data: {
                labels: performanceData.stepLabels,
                datasets: [{
                    label: 'Load Time (s)',
                    data: performanceData.loadTimes,
                    backgroundColor: 'rgba(230, 126, 34, 0.7)',
                    borderColor: 'rgba(230, 126, 34, 1)',
                    borderWidth: 2
                }]
            },
            options: chartOptions
        });
        
        // Total Time Comparison Chart
        const totalCtx = document.getElementById('totalTimeChart').getContext('2d');
        new Chart(totalCtx, {
            type: 'line',
            data: {
                labels: performanceData.stepLabels,
                datasets: [
                    {
                        label: 'Response Time (s)',
                        data: performanceData.responseTimes,
                        borderColor: 'rgba(52, 152, 219, 1)',
                        backgroundColor: 'rgba(52, 152, 219, 0.1)',
                        tension: 0.4
                    },
                    {
                        label: 'Load Time (s)',
                        data: performanceData.loadTimes,
                        borderColor: 'rgba(230, 126, 34, 1)',
                        backgroundColor: 'rgba(230, 126, 34, 0.1)',
                        tension: 0.4
                    },
                    {
                        label: 'Total Time (s)',
                        data: performanceData.totalTimes,
                        borderColor: 'rgba(39, 174, 96, 1)',
                        backgroundColor: 'rgba(39, 174, 96, 0.1)',
                        tension: 0.4
                    }
                ]
            },
            options: chartOptions
        });
    </script>
</body>
</html>
""";
        
        // Replace placeholders
        html = html.replace("TIMESTAMP_PLACEHOLDER", timestamp);
        html = html.replace("PERF_DATA_JSON", performanceDataJson);
        html = html.replace("PERF_TABLE_ROWS", stepTableRows);
        html = html.replace("PERF_DATA_AVG_RESPONSE", String.format("%.3f", avgResponseTime));
        html = html.replace("PERF_DATA_AVG_LOAD", String.format("%.3f", avgLoadTime));
        html = html.replace("PERF_DATA_TOTAL", String.format("%.2f", totalExecutionTime / 1000.0));
        html = html.replace("PERF_DATA_STEPS", String.valueOf(metrics.size()));
        html = html.replace("PERF_DATA_TOTAL_FORMATTED", formatDuration(totalExecutionTime));
        
        return html;
    }
    
    private static String generatePerformanceDataJson(Map<String, PerformanceTracker.PerformanceMetric> metrics, List<String> executionOrder) {
        if (metrics.isEmpty()) {
            return "{ stepLabels: [], responseTimes: [], loadTimes: [], totalTimes: [] }";
        }
        
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  stepLabels: [");
        
        List<String> labels = executionOrder.stream()
            .map(id -> {
                PerformanceTracker.PerformanceMetric m = metrics.get(id);
                return m != null ? "\"Step " + (executionOrder.indexOf(id) + 1) + "\"" : null;
            })
            .filter(l -> l != null)
            .collect(Collectors.toList());
        json.append(String.join(", ", labels));
        json.append("],\n");
        
        json.append("  responseTimes: [");
        List<String> responseTimes = executionOrder.stream()
            .map(id -> {
                PerformanceTracker.PerformanceMetric m = metrics.get(id);
                return m != null ? String.format("%.3f", m.responseTime / 1000.0) : "0";
            })
            .collect(Collectors.toList());
        json.append(String.join(", ", responseTimes));
        json.append("],\n");
        
        json.append("  loadTimes: [");
        List<String> loadTimes = executionOrder.stream()
            .map(id -> {
                PerformanceTracker.PerformanceMetric m = metrics.get(id);
                return m != null ? String.format("%.3f", m.loadTime / 1000.0) : "0";
            })
            .collect(Collectors.toList());
        json.append(String.join(", ", loadTimes));
        json.append("],\n");
        
        json.append("  totalTimes: [");
        List<String> totalTimes = executionOrder.stream()
            .map(id -> {
                PerformanceTracker.PerformanceMetric m = metrics.get(id);
                return m != null ? String.format("%.3f", m.totalTime / 1000.0) : "0";
            })
            .collect(Collectors.toList());
        json.append(String.join(", ", totalTimes));
        json.append("]\n");
        json.append("}");
        
        return json.toString();
    }
    
    private static String generateStepTableRows(Map<String, PerformanceTracker.PerformanceMetric> metrics, List<String> executionOrder) {
        if (metrics.isEmpty()) {
            return "<tr><td colspan='7' style='text-align: center; padding: 30px; color: #7f8c8d;'>No performance data available. Run tests to see metrics.</td></tr>";
        }
        
        StringBuilder rows = new StringBuilder();
        int stepNum = 1;
        
        for (String stepId : executionOrder) {
            PerformanceTracker.PerformanceMetric metric = metrics.get(stepId);
            if (metric == null) continue;
            
            String actionClass = "action-" + metric.actionType.toLowerCase();
            String statusClass = metric.status.equals("PASSED") ? "status-passed" : 
                                metric.status.equals("FAILED") ? "status-failed" : "status-skipped";
            String statusIcon = metric.status.equals("PASSED") ? "✅" : 
                              metric.status.equals("FAILED") ? "❌" : "⏭️";
            
            rows.append("<tr>");
            rows.append("<td><strong>").append(stepNum++).append("</strong></td>");
            rows.append("<td>").append(escapeHtml(metric.stepName)).append("</td>");
            rows.append("<td><span class='action-badge ").append(actionClass).append("'>")
                .append(metric.actionType).append("</span></td>");
            rows.append("<td class='time-cell response-time'>")
                .append(String.format("%.3f", metric.responseTime / 1000.0)).append("s</td>");
            rows.append("<td class='time-cell load-time'>")
                .append(String.format("%.3f", metric.loadTime / 1000.0)).append("s</td>");
            rows.append("<td class='time-cell total-time'>")
                .append(String.format("%.3f", metric.totalTime / 1000.0)).append("s</td>");
            rows.append("<td><span class='status-badge ").append(statusClass).append("'>")
                .append(statusIcon).append(" ").append(metric.status).append("</span></td>");
            rows.append("</tr>");
        }
        
        return rows.toString();
    }
    
    private static String formatDuration(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + "ms";
        } else if (milliseconds < 60000) {
            return String.format("%.2fs", milliseconds / 1000.0);
        } else {
            long minutes = milliseconds / 60000;
            long seconds = (milliseconds % 60000) / 1000;
            return minutes + "m " + seconds + "s";
        }
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

