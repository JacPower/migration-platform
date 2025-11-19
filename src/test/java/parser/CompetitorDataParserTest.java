package parser;

import org.example.dto.input.CompetitorExportDto;
import org.example.dto.input.JobDto;
import org.example.parser.CompetitorDataParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CompetitorDataParser Tests")
class CompetitorDataParserTest {

    @TempDir
    Path tempDir;
    private CompetitorDataParser parser;



    @BeforeEach
    void setUp() {
        parser = new CompetitorDataParser();
    }



    @Test
    @DisplayName("parseJson_shouldReturnCompetitorExportDto_whenValidJsonFileIsPassed")
    void parseJson_shouldReturnCompetitorExportDto_whenValidFileIsPassed() throws IOException {
        String json = createValidJsonExport();
        Path filePath = createTempFile("valid_export.json", json);

        CompetitorExportDto result = parser.parse(filePath.toString());

        assertNotNull(result);
        assertEquals(2, result.getJobs().size());
        assertEquals(1001, result.getJobs().get(0).getJobId());
        assertEquals("Test_Job_1", result.getJobs().get(0).getJobName());
    }



    @Test
    @DisplayName("parseJson_shouldThrowIOException_whenFileDoesNotExist")
    void parse_shouldThrowIOException_whenFileDoesNotExist() {
        String nonExistentPath = "/non/existent/path.json";

        assertThrows(IOException.class, () -> parser.parse(nonExistentPath));
    }



    @Test
    @DisplayName("parseJson_shouldThrowException_whenInvalidJsonIsPassed")
    void parseJson_shouldThrowException_whenInvalidIsPassed() throws IOException {
        String invalidJson = "{ invalid json }";
        Path filePath = createTempFile("invalid.json", invalidJson);

        assertThrows(Exception.class, () -> parser.parse(filePath.toString()));
    }



    @Test
    @DisplayName("parseJson_shouldReturnEmptyJobs_whenJsonContainsEmptyJobsArray")
    void parseJson_shouldReturnEmptyJobs_whenContainsEmptyJobsArray() throws IOException {
        String json = "{\"jobs\": []}";
        Path filePath = createTempFile("empty.json", json);

        CompetitorExportDto result = parser.parse(filePath.toString());

        assertNotNull(result);
        assertTrue(result.getJobs().isEmpty());
    }



    @Test
    @DisplayName("parseJson_shouldIgnoreUnknownProperties_whenJsonContainsUnknownFields")
    void parseJson_shouldIgnoreUnknownProperties_whenContainsUnknownFields() throws IOException {
        String json = """
                {
                    "jobs": [
                        {
                            "jobId": 1001,
                            "jobName": "Test",
                            "unknownField": "value",
                            "trigger": {"type": "SCHEDULE"}
                        }
                    ]
                }
                """;
        Path filePath = createTempFile("unknown_props.json", json);

        CompetitorExportDto result = parser.parse(filePath.toString());

        assertNotNull(result);
        assertEquals(1, result.getJobs().size());
    }



    // JSON string parsing tests
    @Test
    @DisplayName("parseJsonString_shouldReturnCompetitorExportDto_whenValidJsonStringIsPassed")
    void parseJsonString_shouldReturnCompetitorExportDto_whenValidStringIsPassed() throws IOException {
        String json = createValidJsonExport();

        CompetitorExportDto result = parser.parse(json);

        assertNotNull(result);
        assertEquals(2, result.getJobs().size());
    }



    @Test
    @DisplayName("parseJsonString_shouldParseAllFieldsCorrectly_whenCompleteJobJsonIsPassed")
    void parseJsonString_shouldParseAllFieldsCorrectly_whenCompleteJobIsPassed() throws IOException {
        String json = createCompleteJobJson();

        CompetitorExportDto result = parser.parse(json);
        JobDto job = result.getJobs().get(0);

        assertEquals(1001, job.getJobId());
        assertEquals("Complete_Job", job.getJobName());
        assertEquals("BACKUP", job.getJobType());
        assertEquals("ORACLE", job.getSystem());
        assertNotNull(job.getTrigger());
        assertEquals("SCHEDULE", job.getTrigger().getType().name());
        assertEquals("0 2 * * *", job.getTrigger().getCronExpression());
        assertEquals(1, job.getDependencies().size());
        assertNotNull(job.getExecutionConstraints());
        assertEquals(8, job.getExecutionConstraints().getPriority());
    }



    private Path createTempFile(String filename, String content) throws IOException {
        Path filePath = tempDir.resolve(filename);
        Files.writeString(filePath, content);
        return filePath;
    }



    private String createValidJsonExport() {
        return """
                {
                    "jobs": [
                        {
                            "jobId": 1001,
                            "jobName": "Test_Job_1",
                            "jobType": "BACKUP",
                            "system": "ORACLE",
                            "trigger": {"type": "SCHEDULE", "cronExpression": "0 2 * * *"}
                        },
                        {
                            "jobId": 1002,
                            "jobName": "Test_Job_2",
                            "jobType": "TRANSFORM",
                            "system": "SAP",
                            "trigger": {"type": "MANUAL"}
                        }
                    ]
                }
                """;
    }



    private String createCompleteJobJson() {
        return """
                {
                    "jobs": [
                        {
                            "jobId": 1001,
                            "jobName": "Complete_Job",
                            "jobType": "BACKUP",
                            "system": "ORACLE",
                            "trigger": {
                                "type": "SCHEDULE",
                                "cronExpression": "0 2 * * *",
                                "timezone": "UTC"
                            },
                            "dependencies": [
                                {"dependsOnJobId": 1000, "requiredStatus": "SUCCESS"}
                            ],
                            "executionConstraints": {
                                "priority": 8,
                                "maxRuntimeMinutes": 120,
                                "excludeHolidays": true
                            },
                            "notes": "Test notes"
                        }
                    ]
                }
                """;
    }
}