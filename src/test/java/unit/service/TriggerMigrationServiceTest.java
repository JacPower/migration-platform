package unit.service;

import org.example.dto.internal.Trigger;
import org.example.dto.internal.ValidationResult;
import org.example.dto.output.RedwoodJobDto;
import org.example.exception.MigrationException;
import org.example.report.MigrationAnalysis;
import org.example.report.MigrationResult;
import org.example.service.TriggerHandler;
import org.example.service.TriggerMigrationService;
import org.example.service.TriggerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("TriggerMigrationService Tests")
class TriggerMigrationServiceTest {

    private TriggerMigrationService service;



    @BeforeEach
    void setUp() {
        service = new TriggerMigrationService();
    }



    @Test
    void analyze_shouldIdentifySupportedTriggers() {
        List<Trigger> triggers = List.of(
                createScheduleTrigger("Job_1"),
                createManualTrigger("Job_2"),
                createApiTrigger("Job_3")
        );

        MigrationAnalysis analysis = service.analyze(triggers);

        assertEquals(3, analysis.getSupported().size());
        assertEquals(0, analysis.getUnsupported().size());
    }



    @Test
    void analyze_shouldIdentifyUnsupportedTriggers() {
        Trigger unsupported = Trigger.builder()
                .type(TriggerType.APPROVAL)
                .jobName("Approval_Job")
                .build();

        MigrationAnalysis analysis = service.analyze(List.of(unsupported));

        assertEquals(1, analysis.getUnsupported().size());
    }



    @Test
    void analyze_shouldIdentifyInvalidTriggers() {
        Trigger invalidSchedule = Trigger.builder()
                .type(TriggerType.SCHEDULE)
                .jobName("Invalid_Job")
                .build();

        MigrationAnalysis analysis = service.analyze(List.of(invalidSchedule));

        assertEquals(1, analysis.getInvalid().size());
    }



    @Test
    void analyze_shouldHandleEmptyList() {
        MigrationAnalysis analysis = service.analyze(List.of());
        assertEquals(0, analysis.getTotalCount());
    }


    @Test
    void migrate_shouldMigrateScheduleTrigger() throws MigrationException {
        Trigger trigger = createScheduleTrigger("Test_Job");

        RedwoodJobDto result = service.migrate(trigger);

        assertNotNull(result);
        assertEquals("Test_Job", result.getName());
        assertEquals("SCHEDULED", result.getType());
    }



    @Test
    void migrate_shouldMigrateManualTrigger() throws MigrationException {
        Trigger trigger = createManualTrigger("Manual_Job");

        RedwoodJobDto result = service.migrate(trigger);

        assertEquals("Manual_Job", result.getName());
        assertEquals("MANUAL", result.getType());
    }



    @Test
    void migrate_shouldMigrateApiTrigger() throws MigrationException {
        Trigger trigger = createApiTrigger("Api_Job");

        RedwoodJobDto result = service.migrate(trigger);

        assertEquals("Api_Job", result.getName());
        assertEquals("API", result.getType());
    }



    @Test
    void migrate_shouldMigrateDependencyTrigger() throws MigrationException {
        Trigger trigger = createDependencyTrigger("Dependent_Job", 100);

        RedwoodJobDto result = service.migrate(trigger);

        assertEquals("Dependent_Job", result.getName());
    }



    @Test
    void migrate_shouldProduceCorrectRedwoodJobContent() throws MigrationException {
        Trigger trigger = Trigger.builder()
                .type(TriggerType.SCHEDULE)
                .jobName("Daily_Report")
                .cronExpression("0 3 * * *")
                .timezone("Africa/Nairobi")
                .build();

        RedwoodJobDto result = service.migrate(trigger);

        assertEquals("0 3 * * *", result.getTrigger().getSchedule());
        assertEquals("Africa/Nairobi", result.getTrigger().getTimezone());
    }



    @Test
    void migrate_shouldUseUtcAsDefaultTimezone() throws MigrationException {
        Trigger trigger = Trigger.builder()
                .type(TriggerType.SCHEDULE)
                .jobName("Job")
                .cronExpression("0 0 * * *")
                .build();

        RedwoodJobDto result = service.migrate(trigger);

        assertEquals("UTC", result.getTrigger().getTimezone());
    }



    @Test
    void migrate_shouldThrowExceptionForUnsupportedTrigger() {
        Trigger unsupported = Trigger.builder()
                .type(TriggerType.APPROVAL)
                .jobName("Job")
                .build();

        MigrationException ex = assertThrows(
                MigrationException.class,
                () -> service.migrate(unsupported)
        );

        assertTrue(ex.getMessage().contains("No handler"));
    }



    @Test
    void migrate_shouldThrowExceptionForInvalidTrigger() {
        Trigger invalid = Trigger.builder()
                .type(TriggerType.SCHEDULE)
                .jobName("Job")
                .build();

        MigrationException ex = assertThrows(
                MigrationException.class,
                () -> service.migrate(invalid)
        );

        assertTrue(ex.getMessage().contains("Validation failed"));
    }



    @Test
    void migrate_shouldThrowForDependencyWithoutUpstreamId() {
        Trigger trigger = Trigger.builder()
                .type(TriggerType.DEPENDENCY)
                .jobName("Job")
                .build();

        assertThrows(MigrationException.class, () -> service.migrate(trigger));
    }



    @Test
    void migrateAll_shouldMigrateAllValidTriggers() {
        List<Trigger> triggers = List.of(
                createScheduleTrigger("Job_1"),
                createManualTrigger("Job_2"),
                createScheduleTrigger("Job_3")
        );

        MigrationResult result = service.migrateAll(triggers);

        assertEquals(3, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertFalse(result.hasFailures());
    }



    @Test
    void migrateAll_shouldRecordFailuresForInvalidTriggers() {
        List<Trigger> triggers = List.of(
                createScheduleTrigger("Valid_Job"),
                Trigger.builder().type(TriggerType.APPROVAL).jobName("Invalid_Job").build()
        );

        MigrationResult result = service.migrateAll(triggers);

        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertTrue(result.hasFailures());
    }



    @Test
    void migrateAll_shouldContinueAfterFailure() {
        List<Trigger> triggers = List.of(
                createScheduleTrigger("Job_1"),
                Trigger.builder().type(TriggerType.UNKNOWN).jobName("Bad").build(),
                createScheduleTrigger("Job_3")
        );

        MigrationResult result = service.migrateAll(triggers);

        assertEquals(2, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }



    @Test
    void migrateAll_shouldHandleEmptyList() {
        MigrationResult result = service.migrateAll(List.of());

        assertEquals(0, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertFalse(result.hasFailures());
    }



    @Test
    void mockHandlers_shouldDelegateToHandler() throws MigrationException {
        TriggerHandler mockHandler = mock(TriggerHandler.class);
        RedwoodJobDto expectedJob = RedwoodJobDto.builder().name("test").build();

        when(mockHandler.canHandle(any())).thenReturn(true);
        when(mockHandler.validate(any())).thenReturn(new ValidationResult());
        when(mockHandler.migrate(any())).thenReturn(expectedJob);

        TriggerMigrationService serviceWithMock = new TriggerMigrationService(List.of(mockHandler));
        Trigger trigger = createScheduleTrigger("Test");

        RedwoodJobDto result = serviceWithMock.migrate(trigger);

        assertEquals(expectedJob, result);
        verify(mockHandler).migrate(trigger);
    }



    @Test
    void mockHandlers_shouldNotMigrateWhenValidationFails() {
        TriggerHandler mockHandler = mock(TriggerHandler.class);
        ValidationResult failed = new ValidationResult();
        failed.addError("Invalid");

        when(mockHandler.canHandle(any())).thenReturn(true);
        when(mockHandler.validate(any())).thenReturn(failed);

        TriggerMigrationService serviceWithMock = new TriggerMigrationService(List.of(mockHandler));
        Trigger trigger = createScheduleTrigger("Test");

        assertThrows(MigrationException.class, () -> serviceWithMock.migrate(trigger));

        verify(mockHandler, never()).migrate(any());
    }



    @Test
    void mockHandlers_shouldUseFirstMatchingHandler() throws MigrationException {
        TriggerHandler handler1 = mock(TriggerHandler.class);
        TriggerHandler handler2 = mock(TriggerHandler.class);

        when(handler1.canHandle(any())).thenReturn(true);
        when(handler2.canHandle(any())).thenReturn(true);
        when(handler1.validate(any())).thenReturn(new ValidationResult());
        when(handler1.migrate(any())).thenReturn(RedwoodJobDto.builder().name("h1").build());

        TriggerMigrationService serviceWithMock = new TriggerMigrationService(List.of(handler1, handler2));

        serviceWithMock.migrate(createScheduleTrigger("Test"));

        verify(handler1).migrate(any());
        verify(handler2, never()).migrate(any());
    }

    // =====================================================================
    // HELPERS
    // =====================================================================



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



    private Trigger createApiTrigger(String jobName) {
        return Trigger.builder()
                .type(TriggerType.API)
                .jobName(jobName)
                .build();
    }



    private Trigger createDependencyTrigger(String jobName, int upstreamJobId) {
        return Trigger.builder()
                .type(TriggerType.DEPENDENCY)
                .jobName(jobName)
                .upstreamJobId(upstreamJobId)
                .build();
    }
}
