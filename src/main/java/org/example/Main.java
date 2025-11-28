package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.orchestrator.MigrationOrchestrator;
import org.example.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.example.utils.Constants.ARCHIVE_FILE_EXTENSION;
import static org.example.utils.Constants.JSON_FILE_EXTENSION;

@Slf4j
public class Main {


    public static void main(String[] args) throws Exception {
        if (!validateArguments(args)) {
            return;
        }

        String inputPath = args[0];
        String outputPath = args[1];

        if (!areValidPaths(inputPath, outputPath)) {
            return;
        }

        handleMigration(inputPath, outputPath);
    }



    private static boolean validateArguments(String[] args) {
        if (args == null || args.length < 2) {
            log.error("Input and output folder paths arguments are required");
            return false;
        }
        return true;
    }



    private static boolean areValidPaths(String inputPath, String outputPath) {
        if (!FileUtils.isValidDirectoryPathFormat(inputPath) || !FileUtils.isValidDirectoryPathFormat(outputPath)) {
            log.error("Invalid input or output folder path format");
            return false;
        }

        return true;
    }



    private static void handleMigration(String inputPath, String outputPath) throws Exception {
        List<String> files = FileUtils.listFiles(Paths.get(inputPath), JSON_FILE_EXTENSION);

        if (files.isEmpty()) {
            log.info("No export files found for migration: {}", inputPath);
            return;
        }

        log.info("Number of export files to process: {}", files.size());

        try (MigrationOrchestrator orchestrator = new MigrationOrchestrator(outputPath)) {
            orchestrator.migrate(files);
        }

        archiveOutput(outputPath);
    }



    private static void archiveOutput(String outputPath) throws IOException {
        Path outputDir = Paths.get(outputPath);
        Path archivePath = Paths.get(outputPath + ARCHIVE_FILE_EXTENSION);
        FileUtils.zipFolderAndDelete(outputDir, archivePath);
    }
}
