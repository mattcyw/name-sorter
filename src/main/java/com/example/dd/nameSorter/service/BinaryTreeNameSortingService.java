package com.example.dd.nameSorter.service;

import com.example.dd.nameSorter.model.IterativeBinarySearchTree;
import com.example.dd.nameSorter.model.Name;
import jakarta.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.service.type", havingValue = "binaryTree", matchIfMissing = true)
public class BinaryTreeNameSortingService extends FileContentSortingEvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(BinaryTreeNameSortingService.class);

    @Override
    protected Collection<Name> readNamesFromFile(@Nonnull String inputFilePath) {

        final IterativeBinarySearchTree<Name> nameTree = new IterativeBinarySearchTree<Name>();

        final Path path = Paths.get(inputFilePath);

        final AtomicInteger lineCount = new AtomicInteger(0);
        try {
            Files.readAllLines(path)
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
                .forEach(nameTree::insert);

        } catch (IOException e) {
            logger.error("Error reading file: {}", e.getMessage());
            throw new RuntimeException("Failed to read names from file: " + inputFilePath, e);
        }

        return nameTree.traverseInOrder();
    }

    @Override
    protected @Nonnull List<String> writeSortedNamesToFile(@Nonnull Collection<Name> nameList, @Nonnull String outputFilePath) {

        // No sorting is needed here because the names are already sorted in the BST.

        final Path path = Paths.get(outputFilePath);

        try (
            final BufferedWriter writer = Files.newBufferedWriter(
                path,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)
        ) {
            return nameList.stream()
                .map(name -> {
                    try {
                        // Write the line and add a separator
                        writer.write(name.toString());
                        writer.newLine();
                        return name.toString();
                    } catch (IOException e) {
                        // Cannot throw checked exceptions from inside a forEach lambda.
                        // Instead, convert it to a RuntimeException (or log and absorb).
                        throw new UncheckedIOException("Failed to write line to file: " + name.toString(), e);
                    }
                })
                .toList();

        } catch (UncheckedIOException e) {
            throw new RuntimeException("Failed to write sorted names to file: " + outputFilePath, e.getCause());

        } catch (IOException e) {
            throw new RuntimeException("File IO setup/teardown failed for: " + outputFilePath, e);
        }
    }
}
