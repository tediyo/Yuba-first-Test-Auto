package com.example.automation.runners;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty, summary, html:target/cucumber-reports/html-report, json:target/cucumber-reports/cucumber.json")
@ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME, value = "@signin or @performance or @compatibility")
public class CucumberTestRunnerTest {
    // JUnit Platform Suite runner for Cucumber BDD tests
    // Class name ends with "Test" to be discovered by Maven Surefire
    // Runs tests tagged with @signin, @performance, or @compatibility
}

