package com.example.dd.nameSorter.service;

import com.example.dd.nameSorter.model.Name;
import jakarta.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileContentSortingEvaluationService {

    // this.getClass(): to support subclass logging, such that logs show the actual implementing class
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Reads names from the specified input file.
     * Sorting is performed here when using a sorted data structure.
     * @param inputFilePath the path to the input file
     * @return a collection of names read from the file
     */
    protected abstract Collection<Name> readNamesFromFile(@Nonnull String inputFilePath);

    /**
     * Writes the sorted names to the specified output file.
     * Sorting is performed here when not using a sorted data structure.
     * i.e. Sorting happens just before writing to the file.
     * @param nameList the collection of names to write
     * @param outputFilePath the path to the output file
     * @return a list of names written to the file
     */
    protected abstract @Nonnull List<String> writeSortedNamesToFile(@Nonnull Collection<Name> nameList, @Nonnull String outputFilePath);

    /**
     * Sorts names from the input file and writes them to the output file.
     * Performance metrics are logged for reading, writing, and the entire process.
     * @param inputFilePath the path to the input file
     * @param outputFilePath the path to the output file
     */
    public void sortNamesInFile(@Nonnull String inputFilePath, @Nonnull String outputFilePath) {

        long totalMemory = -1;
        long freeMemory = -1;
        long usedMemory = -1;
        long maxMemory = -1;

        // ensure a clean memory state before starting
        Runtime runtime = Runtime.getRuntime();

        if (logger.isInfoEnabled()) {
            totalMemory = runtime.totalMemory();
            freeMemory = runtime.freeMemory();
            usedMemory = totalMemory - freeMemory;
            maxMemory = runtime.maxMemory();
        }

        Instant start = Instant.now();

        logger.info("Reading names from file: {}", inputFilePath);
        Instant readStart = Instant.now();
        Collection<Name> nameList = readNamesFromFile(inputFilePath);
        Instant readEnd = Instant.now();

        logger.info("Write sorted names to file: {}", outputFilePath);
        Instant writeStart = Instant.now();
        List<String> sortedNameList = writeSortedNamesToFile(nameList, outputFilePath);
        Instant writeEnd = Instant.now();

        Instant end = Instant.now();

        // Print to console without affecting performance metrics
        sortedNameList.forEach(System.out::println);

        // Log performance metrics at the end for better readability
        // Assume immaterial logging overhead for simplicity

        if (logger.isInfoEnabled()) {
            logger.info("########### START OF PERFORMANCE METRICS ###########");
            logger.info("PERFORMANCE: Read {} names in {} ms", nameList.size(), Duration.between(readStart, readEnd).toMillis());
            logger.info("PERFORMANCE: Wrote sorted names in {} ms", Duration.between(writeStart, writeEnd).toMillis());
            logger.info("PERFORMANCE: Entire Process completed in {} ms", Duration.between(start, end).toMillis());

            logger.info("PERFORMANCE: JVM Memory Usage BEFORE - Used: {} MB, Free: {} MB, Total: {} MB, Max: {} MB",
                usedMemory / (1024 * 1024),
                freeMemory / (1024 * 1024),
                totalMemory / (1024 * 1024),
                maxMemory / (1024 * 1024));

            totalMemory = runtime.totalMemory();
            freeMemory = runtime.freeMemory();
            usedMemory = totalMemory - freeMemory;
            maxMemory = runtime.maxMemory();
            logger.info("PERFORMANCE: JVM Memory Usage AFTER - Used: {} MB, Free: {} MB, Total: {} MB, Max: {} MB",
                usedMemory / (1024 * 1024),
                freeMemory / (1024 * 1024),
                totalMemory / (1024 * 1024),
                maxMemory / (1024 * 1024));
            logger.info("########### END OF PERFORMANCE METRICS ###########");
        }

    }

}
