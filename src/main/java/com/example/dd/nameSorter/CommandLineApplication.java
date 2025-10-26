package com.example.dd.nameSorter;

import com.example.dd.nameSorter.service.FileContentSortingEvaluationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CommandLineApplication {

    private static final Logger logger = LoggerFactory.getLogger(CommandLineApplication.class);

    private static final int INPUT_FILE_ARG_POS = 1;

    private static final int OUTPUT_FILE_ARG_POS = 2;

    @Autowired
    private FileContentSortingEvaluationService fileContentSortingEvaluationService;

    @Value("${app.input.file}")
    private String inputFilePath;

    @Value("${app.output.file}")
    private String outputFilePath;

    public static void main(String[] args) {
        SpringApplication.run(CommandLineApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            logger.info("Running Name-Sorter CommandLineRunner... Started");

            // override application configured file paths with command line arguments
            if (args.length >= INPUT_FILE_ARG_POS) {
                inputFilePath = args[0];
                logger.info("Input file path: {}", args[0]);
            }

            if (args.length >= OUTPUT_FILE_ARG_POS) {
                outputFilePath = args[1];
                logger.info("Output file path: {}", args[1]);
            }

            // safety check in case application.yml is corrupted
            if (inputFilePath == null || outputFilePath == null) {
                logger.error("Input and output file paths must be provided either as command line arguments or application properties.");
                return;
            }

            // validate input and output file path structure and existence
            try {
                logger.info("Resolved Input file path: {}", inputFilePath);
                final Path inputPath = Paths.get(inputFilePath);

                if (!Files.exists(inputPath)) {
                    logger.error("Input file not found: {}", inputFilePath);
                    return;
                }

                if (!Files.isRegularFile(inputPath) || !Files.isReadable(inputPath)) {
                    logger.error("Input path is not a Readable file: {}", inputFilePath);
                    return;
                }

                try {
                    logger.info("Resolved Output file path: {}", outputFilePath);
                    final Path outputPath = Paths.get(outputFilePath);

                    if (outputPath.getParent() != null) {
                        // create parent directories if they don't exist, such that output file can be created from within
                        if (!Files.exists(outputPath.getParent())) {
                            Files.createDirectory(outputPath.getParent());
                        }
                    }else {
                        // test if output file is writable in advance
                        Files.write(outputPath, "".getBytes(),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE);
                    }
                }catch (IOException e) {
                    logger.error("Output path is not writable: {}; Error: {}", outputFilePath, e.getMessage());
                    return;
                }

            } catch (InvalidPathException e) {
                logger.error("Structural path error: {}", e.getMessage());
                return;

            }

            // core logic
            fileContentSortingEvaluationService.sortNamesInFile(inputFilePath, outputFilePath);

            logger.info("Running Name-Sorter CommandLineRunner... Ended");

        };
    }
}