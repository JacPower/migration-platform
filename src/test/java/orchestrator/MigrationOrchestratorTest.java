package orchestrator;

import org.example.config.MigrationDependencies;
import org.example.dto.input.CompetitorExportDto;
import org.example.dto.input.JobDto;
import org.example.dto.input.TriggerDto;
import org.example.dto.internal.ValidationResult;
import org.example.exception.ValidationException;
import org.example.orchestrator.MigrationOrchestrator;
import org.example.parser.BatchFileParser;
import org.example.parser.DataParser;
import org.example.report.MigrationAnalysis;
import org.example.report.MigrationResult;
import org.example.service.TriggerMigrationService;
import org.example.service.TriggerType;
import org.example.validator.ExportValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MigrationOrchestratorTest {

    private static final ValidationResult VALID = new ValidationResult();
    private DataParser mockDataParser;
    private BatchFileParser mockBatchParser;
    private ExportValidator mockValidator;
    private TriggerMigrationService mockTriggerService;
    private MigrationOrchestrator orchestrator;



    @BeforeEach
    void setUp() {
        mockDataParser = mock(DataParser.class);
        mockBatchParser = mock(BatchFileParser.class);
        mockValidator = mock(ExportValidator.class);
        mockTriggerService = mock(TriggerMigrationService.class);

        MigrationDependencies deps = mock(MigrationDependencies.class);
        when(deps.dataParser()).thenReturn(mockDataParser);
        when(deps.batchFileParser()).thenReturn(mockBatchParser);
        when(deps.exportValidator()).thenReturn(mockValidator);
        when(deps.triggerService()).thenReturn(mockTriggerService);

        orchestrator = new MigrationOrchestrator(deps);
    }



    private JobDto createMockJob() {
        TriggerDto dto = new TriggerDto();
        dto.setType(TriggerType.SCHEDULE);
        dto.setCronExpression("0 2 * * *");
        dto.setTimezone("UTC");

        JobDto job = mock(JobDto.class);
        when(job.getTrigger()).thenReturn(dto);
        when(job.getJobName()).thenReturn("Job_1");
        return job;
    }



    @Test
    void migrate_singleFile_shouldParseValidateAndMigrateTriggers() throws IOException {
        String filePath = "export.json";
        JobDto job = createMockJob();

        CompetitorExportDto export = CompetitorExportDto.builder().jobs(List.of(job)).build();
        MigrationResult result = mock(MigrationResult.class);
        MigrationAnalysis analysis = mock(MigrationAnalysis.class);

        when(mockDataParser.parse(filePath)).thenReturn(export);
        when(mockValidator.validate(export)).thenReturn(VALID);
        when(mockTriggerService.analyze(anyList())).thenReturn(analysis);
        when(mockTriggerService.migrateAll(anyList())).thenReturn(result);

        orchestrator.migrate(List.of(filePath));

        verify(mockDataParser).parse(filePath);
        verify(mockValidator).validate(export);
        verify(mockTriggerService).analyze(anyList());
        verify(analysis).printReport();
        verify(mockTriggerService).migrateAll(anyList());
        verify(result).printReport();
    }



    @Test
    void migrate_singleFile_shouldThrowValidationExceptionOnFailure() throws IOException {
        String filePath = "export.json";
        CompetitorExportDto export = CompetitorExportDto.builder().jobs(List.of()).build();
        ValidationResult failedValidation = new ValidationResult();
        failedValidation.addError("Invalid");

        when(mockDataParser.parse(filePath)).thenReturn(export);
        when(mockValidator.validate(export)).thenReturn(failedValidation);

        assertThrows(ValidationException.class, () -> orchestrator.migrate(List.of(filePath)));
        verify(mockTriggerService, never()).migrateAll(anyList());
    }



    @Test
    void migrate_multipleFiles_shouldCallBatchParser() throws IOException {
        List<String> paths = List.of("file1.json", "file2.json");
        List<JobDto> jobs = List.of(createMockJob());
        when(mockBatchParser.parseMultipleFiles(paths)).thenReturn(CompletableFuture.completedFuture(jobs));
        when(mockValidator.validate(any())).thenReturn(VALID);
        when(mockTriggerService.migrateAll(anyList())).thenReturn(mock(MigrationResult.class));

        orchestrator.migrate(paths);

        verify(mockBatchParser).parseMultipleFiles(paths);
    }



    @Test
    void close_shouldShutdownDependencies() {
        orchestrator.close();

        verify(mockBatchParser).shutdown();
        verify(mockValidator).shutdown();
    }
}
