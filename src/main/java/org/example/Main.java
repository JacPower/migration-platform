package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class Main {

    private static final String API_URL = "https://api.redwood.com";
    private static final Path INPUT_FOLDER = Paths.get("src/main/resources/competitor-exports");



    public static void main(String[] args) throws Exception {
        MigrationApplication app = new MigrationApplication(API_URL);

        //TODO: implement file processing logic
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