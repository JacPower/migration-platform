package service;

import org.example.dto.internal.Trigger;
import org.example.dto.output.RedwoodJobDto;
import org.example.exception.MigrationException;
import org.example.report.MigrationAnalysis;
import org.example.report.MigrationResult;
import org.example.service.TriggerMigrationService;
import org.example.service.TriggerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TriggerMigrationService Tests")
class TriggerMigrationServiceTest {

    private TriggerMigrationService service;



    @BeforeEach
    void setUp() {
        service = new TriggerMigrationService();
    }



    @Test
    void analyze_shouldIdentifySupportedTriggers() {
        List<Trigger> triggers = Arrays.asList(
                createScheduleTrigger("Job_1"),
                createManualTrigger("Job_2")
        );

        MigrationAnalysis analysis = service.analyze(triggers);

        assertEquals(2, analysis.getSupported().size());
        assertEquals(0, analysis.getUnsupported().size());
    }



    @Test
    void analyze_shouldIdentifyUnsupportedTriggers() {
        Trigger unsupported = Trigger.builder()
                .type(TriggerType.APPROVAL)
                .jobName("Approval_Job")
                .build();

        MigrationAnalysis analysis = service.analyze(Collections.singletonList(unsupported));

        assertEquals(0, analysis.getSupported().size());
        assertEquals(1, analysis.getUnsupported().size());
    }



    @Test
    void analyze_shouldIdentifyInvalidTriggers() {
        Trigger invalidSchedule = Trigger.builder()
                .type(TriggerType.SCHEDULE)
                .jobName("Invalid_Job")
                .build();

        MigrationAnalysis analysis = service.analyze(Collections.singletonList(invalidSchedule));

        assertEquals(1, analysis.getInvalid().size());
    }



    @Test
    void analyze_shouldHandleEmptyList() {
        MigrationAnalysis analysis = service.analyze(List.of());
        assertEquals(0, analysis.getTotalCount());
    }



    @Test
    void analyze_shouldCategorizeMixedTriggersCorrectly() {
        List<Trigger> triggers = Arrays.asList(
                createScheduleTrigger("Scheduled"),
                createManualTrigger("Manual"),
                Trigger.builder()
                        .type(TriggerType.FILE_WATCH)
                        .jobName("FileWatch")
                        .watchPath("/data")
                        .build(),
                Trigger.builder()
                        .type(TriggerType.APPROVAL)
                        .jobName("Approval")
                        .build()
        );

        MigrationAnalysis analysis = service.analyze(triggers);

        assertEquals(4, analysis.getTotalCount());
        assertEquals(1, analysis.getUnsupported().size());
    }



    @Test
    void migrate_shouldMigrateValidTrigger() throws MigrationException {
        Trigger trigger = createScheduleTrigger("Test_Job");

        RedwoodJobDto result = service.migrate(trigger);

        assertNotNull(result);
        assertEquals("Test_Job", result.getName());
    }



    @Test
    void migrate_shouldThrowExceptionForUnsupportedTrigger() {
        Trigger unsupported = Trigger.builder()
                .type(TriggerType.APPROVAL)
                .jobName("Job")
                .build();

        MigrationException exception = assertThrows(
                MigrationException.class,
                () -> service.migrate(unsupported)
        );

        assertTrue(exception.getMessage().contains("No handler"));
    }



    @Test
    void migrate_shouldThrowExceptionForInvalidTrigger() {
        Trigger invalid = Trigger.builder()
                .type(TriggerType.SCHEDULE)
                .jobName("Job")
                .build();

        assertThrows(MigrationException.class, () -> service.migrate(invalid));
    }



    @Test
    void migrateAll_shouldMigrateAllValidTriggers() {
        List<Trigger> triggers = Arrays.asList(
                createScheduleTrigger("Job_1"),
                createManualTrigger("Job_2"),
                createScheduleTrigger("Job_3")
        );

        MigrationResult result = service.migrateAll(triggers);

        assertEquals(3, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
    }



    @Test
    void migrateAll_shouldRecordFailuresForInvalidTriggers() {
        List<Trigger> triggers = Arrays.asList(
                createScheduleTrigger("Valid_Job"),
                Trigger.builder()
                        .type(TriggerType.APPROVAL)
                        .jobName("Invalid_Job")
                        .build()
        );

        MigrationResult result = service.migrateAll(triggers);

        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }



    @Test
    void migrateAll_shouldContinueAfterFailure() {
        List<Trigger> triggers = Arrays.asList(
                createScheduleTrigger("Job_1"),
                Trigger.builder().type(TriggerType.UNKNOWN).jobName("Bad").build(),
                createScheduleTrigger("Job_3")
        );

        MigrationResult result = service.migrateAll(triggers);

        assertEquals(2, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }



    private Trigger createScheduleTrigger(String jobName) {
        return Trigger.builder()
                .type(TriggerType.SCHEDULE)
                .jobName(jobName)
                .cronExpression("0 2 * * *")
                .timezone("UTC")
                .build();
    }



    private Trigger createManualTrigger(String jobName) {
        return Trigger.builder()
                .type(TriggerType.MANUAL)
                .jobName(jobName)
                .build();
    }
}
