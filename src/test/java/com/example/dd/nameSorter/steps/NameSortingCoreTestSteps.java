package com.example.dd.nameSorter.steps;

import com.example.dd.nameSorter.service.FileContentSortingEvaluationService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class NameSortingCoreTestSteps {

    private final Map<String, FileContentSortingEvaluationService> services = new HashMap<>();
    private final List<String> serviceAliases = new ArrayList<>();
    private String servicePackage;
    private Path templateDir;
    private Path outputDir;
    private String caseId;
    private String inputFileName;

    @Before
    public void setupTestEnvironment() {
        services.clear();
        serviceAliases.clear();
    }

    @Before // preserve output until next run
    public void cleanupTestEnvironment() throws IOException {
        if (caseId != null && !serviceAliases.isEmpty() && outputDir != null) {
            for (String alias : serviceAliases) {
                String outputFileName = "output-" + caseId + "-" + alias + ".txt";
                Path outputFile = outputDir.resolve(outputFileName);
                Files.deleteIfExists(outputFile);
            }
        }
    }

    @Given("the application is ready for file operations")
    public void application_is_ready() {
        // Semantic step for readability in feature file
    }

    @Given("the service package is {string}")
    public void the_service_package_is(String packageName) {
        this.servicePackage = packageName;
    }

    @And("the testing templates directory is {string}")
    public void the_testing_templates_directory_is(String dirPath) {
        this.templateDir = Paths.get(dirPath);
        assertTrue(Files.exists(this.templateDir), "Testing files directory does not exist: " + dirPath);
    }

    @And("the output files directory is {string}")
    public void the_output_file_directory_is(String dirPath) {
        this.outputDir = Paths.get(dirPath);
        assertTrue(Files.exists(this.outputDir), "Testing files directory does not exist: " + dirPath);
    }

    @Given("the following services are implemented:")
    public void the_following_services_are_implemented(DataTable dataTable) throws Exception {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        rows.forEach(row -> {
            try {
                serviceAliases.add(row.get("serviceAlias"));
                services.put(row.get("serviceAlias"),
                    (FileContentSortingEvaluationService) Class.forName(
                        this.servicePackage+"."+row.get("serviceName")).getDeclaredConstructor().newInstance());
            } catch (ClassNotFoundException |
                     InstantiationException |
                     IllegalAccessException |
                     InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @And("the input file {string} and expected output file {string} are prepared")
    public void the_input_and_expected_output_files_are_prepared(String inputFileName, String expectedOutputFileName) {
        // Cucumber replaces <caseId> from the Examples table, so we receive the final filenames.
        this.inputFileName = inputFileName;
        this.caseId = this.inputFileName.substring("input-".length(), this.inputFileName.lastIndexOf(".txt"));

        // The "prepared" step now verifies that the files actually exist.
        Path inputPath = templateDir.resolve(this.inputFileName);
        Path expectedPath = templateDir.resolve(expectedOutputFileName);
        assertTrue(Files.exists(inputPath), "Input file for case " + caseId + " not found at: " + inputPath);
        assertTrue(Files.exists(expectedPath), "Expected output file for case " + caseId + " not found at: " + expectedPath);
    }

    @When("the sorting logic is executed for all services")
    public void the_sorting_logic_is_executed_for_all_services() {
        for (String alias : serviceAliases) {
            FileContentSortingEvaluationService currentService = services.get(alias);
            String outputFileName = "output-" + caseId + "-" + alias + ".txt";
            Path inputPath = templateDir.resolve(this.inputFileName);
            Path outputPath = outputDir.resolve(outputFileName);
            currentService.sortNamesInFile(inputPath.toString(), outputPath.toString());
        }
    }

    @Then("the output files {string} should be created with content matching {string}")
    public void the_output_files_should_be_created_with_content_matching(String actualFileNameTemplate, String expectedFileName) throws IOException {

        Path expectedPath = templateDir.resolve(expectedFileName);
        List<String> expectedLines = Files.readAllLines(expectedPath);

        // The 'When' step ran all services, so this 'Then' step must validate the output for each service.
        for (String alias : serviceAliases) {
            // We manually replace the <serviceAlias> placeholder to get the actual output file name.
            String actualFileName = actualFileNameTemplate.replace("<serviceAlias>", alias);
            Path actualPath = outputDir.resolve(actualFileName);

            assertTrue(Files.exists(actualPath), "Output file was not created for service [" + alias + "] at: " + actualPath);
            List<String> actualLines = Files.readAllLines(actualPath);
            assertEquals(expectedLines, actualLines, "Output file content for service [" + alias + "] does not match expected.");
        }
    }

}
