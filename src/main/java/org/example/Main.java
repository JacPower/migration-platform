package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.orchestrator.MigrationOrchestrator;
import org.example.utils.FileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class Main {


    public static void main(String[] args) throws Exception {
        if (args == null || args.length < 1) {
            log.error("Input and output folder paths arguments are required");
            return;
        }

        String inputPath = args[0];
        String outputPath = args[1];

        if (!FileUtils.isValidDirectoryPathFormat(inputPath) && !FileUtils.isValidDirectoryPathFormat(outputPath)) {
            log.error("Invalid input or output folder path format");
            return;
        }

        Path path = Paths.get(inputPath);

        List<String> files = FileUtils.listFiles(path, ".json");
        if (files.isEmpty()) {
            log.info("No export files found for migration: {}", inputPath);
            return;
        }

        log.info("Number of export files to process: {}", files.size());
        MigrationOrchestrator migrationOrchestrator = new MigrationOrchestrator(outputPath);
        migrationOrchestrator.migrate(files);
        migrationOrchestrator.close();
    }
}
