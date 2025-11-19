package org.example.utils;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.MigrationException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Stream;

import static org.example.utils.JsonUtils.toJsonString;

@Slf4j
public final class FileUtils {

    private FileUtils() {
    }



    public static boolean fileExists(Path filePath) {
        return Files.exists(filePath) && Files.isRegularFile(filePath);
    }



    public static boolean fileExists(String filePath) {
        return fileExists(Paths.get(filePath));
    }



    public static File getFile(String filePath) {
        return Paths.get(filePath).toFile();
    }



    public static String getFileContent(Path filePath) throws IOException {
        if (Files.notExists(filePath)) {
            throw new IOException("File not found: " + filePath);
        }
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }



    public static String getFileContent(String filePath) throws IOException {
        return getFileContent(Paths.get(filePath));
    }



    public static void appendToFile(Path filePath, String content) throws IOException {
        Path parent = filePath.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }
        Files.writeString(filePath, content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }



    public static void appendToFile(String filePath, String content) {
        try {
            appendToFile(Paths.get(filePath), content);
        } catch (IOException e) {
            log.error("Failed to append content files: {}", filePath, e);
        }
    }



    public static List<String> listFiles(Path folder, String extension) {
        try (Stream<Path> stream = Files.list(folder)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(path -> path.toLowerCase().endsWith(extension.toLowerCase()))
                    .toList();
        } catch (IOException e) {
            log.error("Failed to list files in folder: {}", folder, e);
            return List.of();
        }
    }



    public static void writeToJsonFile(Object dto, String outputFileName, String outputFolder) throws MigrationException{
        String jsonString = JsonUtils.toJsonString(dto);
        if (jsonString == null || jsonString.isBlank()) {
            throw new MigrationException("Failed to convert redwoodJobDto to json string");
        }

        String fileName = (outputFileName != null) ? outputFileName : System.currentTimeMillis() + ".json";
        String outputPath = outputFolder.endsWith("/") ? outputFolder + fileName : outputFolder + "/" + fileName;

        FileUtils.appendToFile(outputPath, jsonString);
    }
}