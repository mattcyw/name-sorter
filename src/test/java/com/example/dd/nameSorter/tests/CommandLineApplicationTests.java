package com.example.dd.nameSorter.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@TestMethodOrder(OrderAnnotation.class)
@DisplayNameGeneration(DisplayNameGenerator.Standard.class)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@SuppressWarnings("PMD.AvoidDuplicateLiterals") // Test Cases should be evaluated separately
public class CommandLineApplicationTests { 

    private static final Path TEMPLATE_DIR = Path.of("src/test/resources/files");
    private static final Path OUTPUT_DIR = Path.of("files");

    private static final Path DEFAULT_INPUT = TEMPLATE_DIR.resolve("unsorted-names-list.txt");
    private static final Path CUSTOM_INPUT = TEMPLATE_DIR.resolve("input.txt");
    private static final Path EXPECTED_DEFAULT_OUTPUT = TEMPLATE_DIR.resolve("sorted-names-list-expected.txt");
    private static final Path EXPECTED_CUSTOM_OUTPUT = TEMPLATE_DIR.resolve("output-expected.txt");
    private static final Path DEFAULT_OUTPUT = OUTPUT_DIR.resolve("sorted-names-list.txt");
    private static final Path CUSTOM_OUTPUT = OUTPUT_DIR.resolve("output.txt");
    
    @Autowired
    private ApplicationContext context;

    // Utility method to run the application
    private void runApplication(String[] args) throws Exception {
        context.getBean(CommandLineRunner.class).run(args);
    }
    
    // Utility for content assertion
    private void assertOutputContent(Path actualPath, Path expectedPath) throws IOException {
        assertTrue(Files.exists(actualPath), "Actual output file was not created: " + actualPath);
        
        String actualContent = Files.readString(actualPath).trim();
        String expectedContent = Files.readString(expectedPath).trim();

        assertEquals(expectedContent, actualContent, "Generated output content does not match the expected content.");
    }

    // --- SETUP / CLEANUP ---
    @BeforeEach
    void setupFiles() throws IOException {

        // 1. CLEANUP PRE-RUN
        Files.deleteIfExists(DEFAULT_OUTPUT); // Remove generated files
        Files.deleteIfExists(CUSTOM_OUTPUT);

        // 2. List of files to ensure existence
        Path[] presetFiles = {DEFAULT_INPUT, EXPECTED_DEFAULT_OUTPUT, CUSTOM_INPUT, EXPECTED_CUSTOM_OUTPUT};
        for (Path filePath : presetFiles) {
            assertTrue(Files.exists(filePath), "Expected file does not exists before test: " + filePath);
        }
    }


    // --- TEST CASES ---

    // Example 1: Use Defaults (No Args)
    @Test
    @Order(1)
    void testDefaultInputAndOutput(final CapturedOutput output) throws Exception {
        runApplication(new String[]{});

        assertFalse(output.getOut().contains("ERROR"), "Unexpected Error log found: " + output.getOut());
        assertOutputContent(DEFAULT_OUTPUT, EXPECTED_DEFAULT_OUTPUT);
    }

    // Example 2: Override Both
    @Test
    @Order(2)
    void testOverrideInputAndOutput(final CapturedOutput output) throws Exception {
        runApplication(new String[]{CUSTOM_INPUT.toString(), CUSTOM_OUTPUT.toString()});

        assertFalse(output.getOut().contains("ERROR"), "Unexpected Error log found: " + output.getOut());
        assertOutputContent(CUSTOM_OUTPUT, EXPECTED_CUSTOM_OUTPUT);
    }

    // Example 3: Structurally Invalid Input Path
    @Test
    @Order(3)
    void testFailureOnStructuralInvalidInputPath(final CapturedOutput output) throws Exception {
        runApplication(new String[]{"files/in:put.txt", DEFAULT_OUTPUT.toString()});
        
        assertTrue(output.getOut().contains("Structural path error"), "Expected structural error log.");
        assertFalse(Files.exists(DEFAULT_OUTPUT), "Output file should not be created.");
        assertFalse(Files.exists(CUSTOM_OUTPUT), "Output file should not be created.");
    }

    // Example 4: Structurally Invalid Output Path
    @Test
    @Order(4)
    void testFailureOnStructuralInvalidOutputPath(final CapturedOutput output) throws Exception {
        runApplication(new String[]{DEFAULT_INPUT.toString(), "files/out:put.txt"});
        
        assertTrue(output.getOut().contains("Structural path error"), "Expected structural error log.");
        assertFalse(Files.exists(DEFAULT_OUTPUT), "Output file should not be created.");
        assertFalse(Files.exists(CUSTOM_OUTPUT), "Output file should not be created.");
    }

    // Example 5: Non-Readable Input File Path
    @Test
    @Order(5)
    void testFailureOnNonReadableInputFile(final CapturedOutput output) throws Exception {
        runApplication(new String[]{TEMPLATE_DIR.toString()}); // Passing the output file as input

        assertTrue(output.getOut().contains("Input path is not a Readable file"));
        assertFalse(Files.exists(DEFAULT_OUTPUT), "Output file should not be created.");
        assertFalse(Files.exists(CUSTOM_OUTPUT), "Output file should not be created.");
    }

    // Example 6: Non-Writable Output File Path
    @Test
    @Order(6)
    void testFailureOnNonWritableOutputFile(final CapturedOutput output) throws Exception {
        runApplication(new String[]{DEFAULT_INPUT.toString(), "/////files/non-exists-output.txt"});
        
        assertTrue(output.getOut().contains("Output path is not writable"));
        assertFalse(Files.exists(OUTPUT_DIR.resolve("non-exists-output.txt")), "Output file should not be created.");
    }
    
    // Example 7: Non-Existence Input File (Testing the !Files.exists(inputPath) check)
    @Test
    @Order(7)
    void testFailureOnNonExistenceInputFile(final CapturedOutput output) throws Exception {
        // Use a genuinely non-existent path
        Path nonExistent = TEMPLATE_DIR.resolve("definitely-not-here.txt");
        runApplication(new String[]{nonExistent.toString(), DEFAULT_OUTPUT.toString()});
        
        assertTrue(output.getOut().contains("Input file not found"), "Expected 'Input file not found' log.");
        assertFalse(Files.exists(DEFAULT_OUTPUT), "Output file should not be created.");
        assertFalse(Files.exists(CUSTOM_OUTPUT), "Output file should not be created.");
    }
}