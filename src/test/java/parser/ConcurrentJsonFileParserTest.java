package parser;

import org.example.dto.input.JobDto;
import org.example.parser.ConcurrentJsonFileParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ConcurrentJsonFileParser Tests")
class ConcurrentJsonFileParserTest {

    @TempDir
    Path tempDir;
    private ConcurrentJsonFileParser parser;



    @BeforeEach
    void setUp() {
        parser = new ConcurrentJsonFileParser();
    }



    @AfterEach
    void tearDown() {
        parser.shutdown();
    }



    @Test
    @DisplayName("parseMultipleFiles_shouldReturnAllJobs_whenAllFilesAreValid")
    void parseMultipleFiles_shouldReturnAllJobs_whenAllFilesAreValid() throws Exception {
        Path file1 = createTestFile("file1.json", 1001, "Job_1");
        Path file2 = createTestFile("test2.json", 1002, "Job_2");
        Path file3 = createTestFile("file3.json", 1003, "Job_3");

        List<String> filePaths = List.of(file1.toString(), file2.toString(), file3.toString());

        CompletableFuture<List<JobDto>> future = parser.parseMultipleFiles(filePaths);
        List<JobDto> result = future.get(10, TimeUnit.SECONDS);

        assertEquals(3, result.size());
    }



    @Test
    @DisplayName("parseMultipleFiles_shouldReturnEmptyList_whenFileListIsEmpty")
    void parseMultipleFiles_shouldReturnEmptyList_whenFileListIsEmpty() throws Exception {
        List<String> emptyList = List.of();

        CompletableFuture<List<JobDto>> future = parser.parseMultipleFiles(emptyList);
        List<JobDto> result = future.get(5, TimeUnit.SECONDS);

        assertTrue(result.isEmpty());
    }



    @Test
    @DisplayName("parseMultipleFiles_shouldSkipFailedFiles_andReturnValidJobs")
    void parseMultipleFiles_shouldSkipFailedFiles_andReturnValidJobs() throws Exception {
        Path validFile = createTestFile("valid.json", 1001, "Valid_Job");
        String invalidPath = "/non/existent/file.json";

        List<String> filePaths = List.of(validFile.toString(), invalidPath);

        CompletableFuture<List<JobDto>> future = parser.parseMultipleFiles(filePaths);
        List<JobDto> result = future.get(10, TimeUnit.SECONDS);

        assertEquals(1, result.size());
        assertEquals("Valid_Job", result.get(0).getJobName());
    }



    @Test
    @DisplayName("parseMultipleFiles_shouldCompleteWithinTimeout_whenParsingLargeBatch")
    void parseMultipleFiles_shouldCompleteWithinTimeout_whenParsingLargeBatch() throws Exception {
        List<String> filePaths = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Path file = createTestFile("file" + i + ".json", 1000 + i, "Job_" + i);
            filePaths.add(file.toString());
        }

        long startTime = System.currentTimeMillis();
        CompletableFuture<List<JobDto>> future = parser.parseMultipleFiles(filePaths);
        List<JobDto> result = future.get(30, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - startTime;

        assertEquals(20, result.size());
        System.out.println("Parsed 20 files in " + duration + "ms");
    }



    private Path createTestFile(String filename, int jobId, String jobName) throws IOException {
        String json = String.format("""
                {
                    "jobs": [
                        {
                            "jobId": %d,
                            "jobName": "%s",
                            "jobType": "BACKUP",
                            "system": "ORACLE",
                            "trigger": {"type": "SCHEDULE", "cronExpression": "0 2 * * *"}
                        }
                    ]
                }
                """, jobId, jobName);

        Path filePath = tempDir.resolve(filename);
        Files.writeString(filePath, json);
        return filePath;
    }
}

