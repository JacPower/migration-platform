package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.orchestrator.MigrationOrchestrator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class Main {
    private static final Path INPUT_FOLDER = Paths.get("src/main/resources/competitor-exports");



    public static void main(String[] args) throws Exception {
        List<String> files = loadExportFiles();
        if (files.isEmpty()) {
            log.info("No export files found for migration: {}", INPUT_FOLDER.toAbsolutePath());
            return;
        }

        log.info("Number of export files to process: {}", files.size());
        MigrationOrchestrator migrationOrchestrator = new MigrationOrchestrator();
        migrationOrchestrator.migrate(files);
        migrationOrchestrator.close();
    }



    private static List<String> loadExportFiles() {
        try (Stream<Path> stream = Files.list(Main.INPUT_FOLDER)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(string -> string.toLowerCase().endsWith(".json"))
                    .toList();
        } catch (IOException e) {
          log.error("Failed to load export files: ", e);
          return List.of();
        }
    }
}
