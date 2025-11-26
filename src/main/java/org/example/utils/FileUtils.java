package org.example.utils;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.MigrationException;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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



    public static void writeToJsonFile(Object dto, String outputFileName, String outputFolder) throws MigrationException {
        String jsonString = JsonUtils.toJsonString(dto);
        if (jsonString == null || jsonString.isBlank()) {
            throw new MigrationException("Failed to convert redwoodJobDto to json string");
        }

        String fileName = (outputFileName != null) ? outputFileName : System.currentTimeMillis() + ".json";
        String outputPath = outputFolder.endsWith("/") ? outputFolder + fileName : outputFolder + "/" + fileName;

        FileUtils.appendToFile(outputPath, jsonString);
    }



    public static boolean isValidDirectoryPathFormat(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }

        try {
            Paths.get(path.trim());
            return true;
        } catch (InvalidPathException e) {
            return false;
        }
    }



    public static void zipFolderAndDelete(Path sourceFolder, Path zipFile) throws IOException {

        Files.createDirectories(zipFile.getParent());

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {

            try (var paths = Files.walk(sourceFolder)) {
                paths.forEach(path -> {
                    try {
                        if (Files.isDirectory(path)) return;

                        Path relative = sourceFolder.relativize(path);
                        zos.putNextEntry(new ZipEntry(relative.toString()));
                        Files.copy(path, zos);
                        zos.closeEntry();

                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
        }

        //deleteFolderRecursively(sourceFolder);
    }



    private static void deleteFolderRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;

        try (var walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
    }
}