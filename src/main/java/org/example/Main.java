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
            log.error("No export files found in the input folder: {}", INPUT_FOLDER.toAbsolutePath());
            return;
        }

        log.info("Number of export files to process: {}", files.size());
        MigrationOrchestrator migrationOrchestrator = new MigrationOrchestrator();

        if (files.size() == 1) {
            migrationOrchestrator.migrate(files.get(0));
        } else {
            migrationOrchestrator.migrateAsync(files);
        }

        migrationOrchestrator.shutdown();
    }



    private static List<String> loadExportFiles() throws IOException {
        try (Stream<Path> stream = Files.list(Main.INPUT_FOLDER)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(string -> string.toLowerCase().endsWith(".json"))
                    .toList();
        }
    }
}
