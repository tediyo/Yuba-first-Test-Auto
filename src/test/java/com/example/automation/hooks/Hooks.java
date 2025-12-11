package com.example.automation.hooks;

import com.example.automation.reporting.CustomHtmlReportGenerator;
import com.example.automation.support.DriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import org.openqa.selenium.WebDriver;
import java.time.LocalDateTime;

public class Hooks {

    private static boolean driverInitialized = false;
    private static LocalDateTime testExecutionStart;

    @BeforeAll
    public static void setUpOnce() {
        // Record test execution start time
        testExecutionStart = LocalDateTime.now();
        CustomHtmlReportGenerator.setTestExecutionStartTime(testExecutionStart);
        
        // Initialize driver only once for all scenarios
        if (!driverInitialized) {
            DriverFactory.initDriver();
            driverInitialized = true;
        }
    }

    @Before
    public void setUp() {
        // Ensure driver is initialized (fallback if BeforeAll doesn't work)
        if (!driverInitialized) {
            DriverFactory.initDriver();
            driverInitialized = true;
        }
        
        // Clear cookies between scenarios but keep browser open
        WebDriver driver = DriverFactory.getDriver();
        if (driver != null) {
            driver.manage().deleteAllCookies();
            // Navigate to blank page to reset state
            driver.navigate().to("about:blank");
        }
    }

    @After
    public void tearDown() {
        // Don't quit driver after each scenario - keep it open for next scenario
        // Just clear any temporary state if needed
    }

    @AfterAll
    public static void tearDownOnce() {
        // Record test execution end time
        LocalDateTime testExecutionEnd = LocalDateTime.now();
        CustomHtmlReportGenerator.setTestExecutionEndTime(testExecutionEnd);
        
        // Get all test results to determine which tags/categories were executed
        // IMPORTANT: Only check categories, NOT step names, to avoid false positives
        // (e.g., ARefresh scenario uses "ACM navigation link" but is not an ACM test)
        java.util.Map<String, com.example.automation.reporting.TestResultsCollector.TestResult> allResults = 
            com.example.automation.reporting.TestResultsCollector.getAllResults();
        
        // Determine which categories have test results - this is the PRIMARY way to detect executed tags
        java.util.Set<String> executedCategories = allResults.values().stream()
            .map(r -> r.category != null && !r.category.isEmpty() ? r.category : null)
            .filter(cat -> cat != null)
            .collect(java.util.stream.Collectors.toSet());
        
        // Count test results per category for debugging
        long acmResultCount = allResults.values().stream()
            .filter(r -> "ACM".equals(r.category))
            .count();
        long arefreshResultCount = allResults.values().stream()
            .filter(r -> "ARefresh".equals(r.category))
            .count();
        long simResultCount = allResults.values().stream()
            .filter(r -> "SIM".equals(r.category))
            .count();
        
        // If no test results recorded (tests failed early), check PerformanceTracker metrics as fallback
        // But ONLY check for specific patterns that indicate the tag was executed
        boolean hasAcmTests = acmResultCount > 0;
        boolean hasARefreshTests = arefreshResultCount > 0;
        boolean hasSimTests = simResultCount > 0;
        
        // Fallback: If no test results but we have metrics, check for tag-specific patterns
        if (allResults.isEmpty()) {
            java.util.Map<String, com.example.automation.reporting.PerformanceTracker.PerformanceMetric> allMetrics = 
                com.example.automation.reporting.PerformanceTracker.getAllMetrics();
            
            // Check for ARefresh-specific patterns (refresh button, ARefresh verification)
            // Only check for very specific patterns to avoid false positives
            boolean hasRefreshButtonStep = allMetrics.values().stream()
                .anyMatch(m -> m.stepName.toLowerCase().contains("refresh button") || 
                              m.stepName.contains("ARefresh Test - Page Refresh Verification"));
            
            // Check for ACM-specific patterns (ACM navigation, ACM element, ACM verification)
            // Only check for very specific patterns
            boolean hasAcmSpecificSteps = allMetrics.values().stream()
                .anyMatch(m -> (m.stepName.contains("ACM navigation link") || 
                               m.stepName.contains("ACM element") ||
                               m.stepName.contains("ACM Test - New Page Load Verification")) &&
                              !m.stepName.contains("ARefresh")); // Exclude ARefresh scenarios
            
            // Check for SIM-specific patterns
            // SIM tests use steps like "Click element", "Click button", "Enter email in individual email field", "Click form submit button"
            // Look for steps that are specific to SIM workflow (individual email field, form submit button)
            boolean hasSimSpecificSteps = allMetrics.values().stream()
                .anyMatch(m -> (m.stepName.contains("individual email field") || 
                               m.stepName.contains("form submit button") ||
                               m.stepName.contains("SIM Test - Complete User Invitation Verification")) &&
                              !m.stepName.contains("ACM") && 
                              !m.stepName.contains("ARefresh"));
            
            // Use fallback detection only if we have metrics but no test results
            if (!allMetrics.isEmpty()) {
                hasARefreshTests = hasARefreshTests || hasRefreshButtonStep;
                hasAcmTests = hasAcmTests || (hasAcmSpecificSteps && !hasRefreshButtonStep);
                hasSimTests = hasSimTests || hasSimSpecificSteps;
                
                System.out.println("⚠️  No test results recorded, using PerformanceTracker metrics as fallback");
                System.out.println("   Refresh button step found: " + hasRefreshButtonStep);
                System.out.println("   ACM-specific steps found: " + hasAcmSpecificSteps);
                System.out.println("   SIM-specific steps found: " + hasSimSpecificSteps);
            }
        }
        
        System.out.println("=== Report Generation Summary ===");
        System.out.println("Total test results: " + allResults.size());
        System.out.println("Detected categories: " + executedCategories);
        System.out.println("ACM test results count: " + acmResultCount + " -> Generate report: " + hasAcmTests);
        System.out.println("ARefresh test results count: " + arefreshResultCount + " -> Generate report: " + hasARefreshTests);
        System.out.println("SIM test results count: " + simResultCount + " -> Generate report: " + hasSimTests);
        System.out.println("=================================");
        
        // Generate custom HTML report (always generate - it's the main summary report)
        // This shows all test results regardless of category
        try {
            com.example.automation.reporting.CustomHtmlReportGenerator.generateCustomReport();
            System.out.println("✅ Custom report (yuba-test-report.html) generated");
        } catch (Exception e) {
            System.err.println("Failed to generate custom report: " + e.getMessage());
        }
        
        // Generate reports ONLY for executed tags/categories (strict category-based detection)
        // Only generate if there are actual test results with that category
        if (hasSimTests) {
            try {
                com.example.automation.reporting.SimHtmlReportGenerator.generateSimReport();
                System.out.println("✅ SIM report (sim_report.html) generated");
            } catch (Exception e) {
                System.err.println("Failed to generate SIM report: " + e.getMessage());
            }
        } else {
            System.out.println("⏭️  Skipping SIM report (no SIM category tests executed - found " + simResultCount + " results)");
        }
        
        if (hasAcmTests) {
            try {
                com.example.automation.reporting.AcmHtmlReportGenerator.generateAcmReport();
                System.out.println("✅ ACM report (acm_report.html) generated");
            } catch (Exception e) {
                System.err.println("Failed to generate ACM report: " + e.getMessage());
            }
        } else {
            System.out.println("⏭️  Skipping ACM report (no ACM category tests executed - found " + acmResultCount + " results)");
        }
        
        if (hasARefreshTests) {
            try {
                com.example.automation.reporting.ARefreshHtmlReportGenerator.generateARefreshReport();
                System.out.println("✅ ARefresh report (arefresh_report.html) generated");
            } catch (Exception e) {
                System.err.println("Failed to generate ARefresh report: " + e.getMessage());
            }
        } else {
            System.out.println("⏭️  Skipping ARefresh report (no ARefresh category tests executed - found " + arefreshResultCount + " results)");
        }
        
        // Quit driver only once at the end of all scenarios
        if (driverInitialized) {
            DriverFactory.quitDriver();
            driverInitialized = false;
        }
    }
}

