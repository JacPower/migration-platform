package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.orchestrator.MigrationOrchestrator;
import org.example.utils.Constants;
import org.example.utils.FileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class Main {



    public static void main(String[] args) throws Exception {
        String inputFolder = args.length > 0 ? args[0] : Constants.DEFAULT_INPUT_FOLDER;
        Path inputPath = Paths.get(inputFolder);

        List<String> files = FileUtils.listFiles(inputPath, ".json");
        if (files.isEmpty()) {
            log.info("No export files found for migration: {}", inputPath);
            return;
        }

        log.info("Number of export files to process: {}", files.size());
        MigrationOrchestrator migrationOrchestrator = new MigrationOrchestrator();
        migrationOrchestrator.migrate(files);
        migrationOrchestrator.close();
    }
}
