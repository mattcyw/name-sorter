package com.example.dd.nameSorter.service;

import com.example.dd.nameSorter.model.Name;
import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.service.type", havingValue = "collection", matchIfMissing = false)
public class CollectionNameSortingService extends FileContentSortingEvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(CollectionNameSortingService.class);

    @Override
    protected Collection<Name> readNamesFromFile(@Nonnull String inputFilePath) {

        final Path path = Paths.get(inputFilePath);

        final AtomicInteger lineCount = new AtomicInteger(0);
        try {
            return Files.readAllLines(path)
                .stream()
                .filter(line -> {
                    lineCount.incrementAndGet();
                    final boolean isEmpty = line.trim().isEmpty();
                    if (isEmpty) {
                        logger.warn("Skipping empty line: {}", lineCount.get());
                    }
                    return !isEmpty;
                })
                .map(fullName -> {
                    try {
                        return new Name(fullName);
                    } catch (IllegalArgumentException e) {
                        logger.warn("Skipping line: {}. Invalid name format: {}", lineCount.get(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        } catch (IOException e) {
            logger.error("Error reading file: {}", e.getMessage());
            throw new RuntimeException("Failed to read names from file: " + inputFilePath, e);
        }

    }

    @Override
    protected @Nonnull List<String> writeSortedNamesToFile(@Nonnull Collection<Name> nameList, @Nonnull String outputFilePath) {

        final Path path = Paths.get(outputFilePath);

        final List<String> lines = nameList.stream()
            .sorted() // Sort the names here before writing
            .map(Name::toString)
            .toList();

        try {
            Files.write(path, lines,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            logger.error("Error writing file: {}", e.getMessage());
            throw new RuntimeException("Failed to write sorted names to file: " + outputFilePath, e);
        }

        return lines;
    }
}
