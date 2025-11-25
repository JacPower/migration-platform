package integration;

import org.example.Main;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static unit.TestDataConstants.*;
import static unit.TestDataConstants.FileNames.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MainIntegrationTest {



    @TempDir
    Path tempDir;

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private PrintStream originalErr;



    @BeforeEach
    void setUp() throws IOException {
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outputStream));

        Files.createDirectories(ACTUAL_OUTPUT_DIR);
        cleanupOutputDirectory();
    }



    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }



    @Test
    @Order(1)
    @DisplayName("End-to-end test with single Oracle backup job")
    void testSingleOracleBackupJob() throws Exception {
        Path inputDir = tempDir.resolve("input");
        Files.createDirectories(inputDir);

        createTestJsonFile(inputDir, ORACLE_BACKUP, SINGLE_ORACLE_BACKUP_JOB);

        Main.main(new String[]{inputDir.toString()});

        String consoleOutput = outputStream.toString();
        assertTrue(consoleOutput.contains("Number of export files to process: 1"));
        assertTrue(Files.list(ACTUAL_OUTPUT_DIR).count() > 0);
    }



    @Test
    @Order(3)
    @DisplayName("Should process simple dependency chain")
    void testSimpleDependencyChain() throws Exception {
        Path inputDir = tempDir.resolve("input");
        Files.createDirectories(inputDir);
        cleanupOutputDirectory();

        createTestJsonFile(inputDir, DEPENDENCY_CHAIN, SIMPLE_DEPENDENCY_CHAIN);

        Main.main(new String[]{inputDir.toString()});

        String consoleOutput = outputStream.toString();
        assertTrue(consoleOutput.contains("Number of export files to process: 1"));
        assertTrue(Files.list(ACTUAL_OUTPUT_DIR).count() > 0);
    }



    @Test
    @Order(5)
    @DisplayName("Should process multiple different job export files")
    void testMultipleJobExports() throws Exception {
        Path inputDir = tempDir.resolve("input");
        Files.createDirectories(inputDir);
        cleanupOutputDirectory();

        createTestJsonFile(inputDir, ORACLE_BACKUP, SINGLE_ORACLE_BACKUP_JOB);
        createTestJsonFile(inputDir, FILE_WATCH_TRIGGER, FILE_WATCH_TRIGGER_JOB);
        createTestJsonFile(inputDir, WEEKLY_MAINTENANCE, WEEKLY_MAINTENANCE_JOB);

        Main.main(new String[]{inputDir.toString()});

        String consoleOutput = outputStream.toString();
        assertTrue(consoleOutput.contains("Number of export files to process: 3"));
        assertTrue(Files.list(ACTUAL_OUTPUT_DIR).count() > 0);
    }



    @Test
    @Order(7)
    @DisplayName("Should process file watch trigger job")
    void testFileWatchTrigger() throws Exception {
        Path inputDir = tempDir.resolve("input");
        Files.createDirectories(inputDir);
        cleanupOutputDirectory();

        createTestJsonFile(inputDir, FILE_WATCH_TRIGGER, FILE_WATCH_TRIGGER_JOB);

        Main.main(new String[]{inputDir.toString()});

        String consoleOutput = outputStream.toString();
        assertTrue(consoleOutput.contains("Number of export files to process: 1"));
        assertTrue(Files.list(ACTUAL_OUTPUT_DIR).count() > 0);
    }



    @Test
    @Order(8)
    @DisplayName("Should process manual trigger job")
    void testManualTrigger() throws Exception {
        Path inputDir = tempDir.resolve("input");
        Files.createDirectories(inputDir);
        cleanupOutputDirectory();

        createTestJsonFile(inputDir, MANUAL_TRIGGER, MANUAL_TRIGGER_JOB);

        Main.main(new String[]{inputDir.toString()});

        String consoleOutput = outputStream.toString();
        assertTrue(consoleOutput.contains("Number of export files to process: 1"));
        assertTrue(Files.list(ACTUAL_OUTPUT_DIR).count() > 0);
    }



    @Test
    @Order(9)
    @DisplayName("Should process API webhook trigger job")
    void testAPIWebhookTrigger() throws Exception {
        Path inputDir = tempDir.resolve("input");
        Files.createDirectories(inputDir);
        cleanupOutputDirectory();

        createTestJsonFile(inputDir, API_WEBHOOK, API_WEBHOOK_TRIGGER_JOB);

        Main.main(new String[]{inputDir.toString()});

        String consoleOutput = outputStream.toString();
        assertTrue(consoleOutput.contains("Number of export files to process: 1"));
        assertTrue(Files.list(ACTUAL_OUTPUT_DIR).count() > 0);
    }



    @Test
    @Order(11)
    @DisplayName("Should handle empty jobs array")
    void testEmptyJobsArray() throws Exception {
        Path inputDir = tempDir.resolve("input");
        Files.createDirectories(inputDir);

        createTestJsonFile(inputDir, "empty_jobs.json", EMPTY_JOBS_ARRAY);

        assertDoesNotThrow(() -> Main.main(new String[]{inputDir.toString()}));
    }



    private void createTestJsonFile(Path directory, String filename, String content)
            throws IOException {
        Path file = directory.resolve(filename);
        Files.writeString(file, content,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }



    private void cleanupOutputDirectory() throws IOException {
        if (Files.exists(ACTUAL_OUTPUT_DIR)) {
            try (var stream = Files.list(ACTUAL_OUTPUT_DIR)) {
                stream.filter(Files::isRegularFile)
                        .forEach(file -> {
                            try {
                                Files.delete(file);
                            } catch (IOException e) {
                                System.err.println("Failed to delete: " + file);
                            }
                        });
            }
        }
    }
}