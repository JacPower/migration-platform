package unit.handler;

import org.example.dto.internal.Trigger;
import org.example.dto.internal.ValidationResult;
import org.example.dto.output.RedwoodJobDto;
import org.example.exception.MigrationException;
import org.example.handler.FileWatchTriggerHandler;
import org.example.service.TriggerType;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileWatchTriggerHandler Tests")
class FileWatchTriggerHandlerTest {

    private FileWatchTriggerHandler handler;

    @BeforeEach
    void setUp() {
        handler = new FileWatchTriggerHandler();
    }

    @Test
    void getSupportedType_shouldReturnFileWatchType() {
        assertEquals(TriggerType.FILE_WATCH, handler.getSupportedType());
    }

    @Test
    void canHandle_shouldReturnTrue_forFileWatchTrigger() {
        Trigger trigger = Trigger.builder().type(TriggerType.FILE_WATCH).build();
        assertTrue(handler.canHandle(trigger));
    }

    @Test
    void validate_shouldFail_whenWatchPathIsMissing() {
        Trigger trigger = Trigger.builder().type(TriggerType.FILE_WATCH).build();
        ValidationResult result = handler.validate(trigger);
        assertTrue(result.hasErrors());
    }

    @Test
    void validate_shouldAddWarning_forPollingWorkaround() {
        Trigger trigger = Trigger.builder()
                .type(TriggerType.FILE_WATCH)
                .watchPath("/data/files/*.csv")
                .build();

        ValidationResult result = handler.validate(trigger);

        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
                .anyMatch(w -> w.contains("polling")));
    }

    @Test
    void migrate_shouldConvertToScheduled_withPollingScript() throws MigrationException {
        Trigger trigger = Trigger.builder()
                .type(TriggerType.FILE_WATCH)
                .jobName("File_Watch_Job")
                .watchPath("/data/incoming/file.csv")
                .build();

        RedwoodJobDto result = handler.migrate(trigger);

        assertNotNull(result);
        assertEquals("File_Watch_Job", result.getName());
        assertEquals("SCHEDULED", result.getType());
        assertEquals("*/5 * * * *", result.getTrigger().getSchedule());
        assertNotNull(result.getTrigger().getPreScript());
        assertTrue(result.getTrigger().getPreScript().contains("/data/incoming/file.csv"));
    }

    @Test
    void migrate_shouldAddMetadata_aboutOriginalTrigger() throws MigrationException {
        Trigger trigger = Trigger.builder()
                .type(TriggerType.FILE_WATCH)
                .jobName("Job")
                .watchPath("/data/file.csv")
                .build();

        RedwoodJobDto result = handler.migrate(trigger);

        assertEquals("FILE_WATCH", result.getMetadata().get("original_trigger_type"));
        assertEquals("POLLING_WORKAROUND", result.getMetadata().get("migration_strategy"));
    }

    @Test
    void migrate_shouldAddNotes_aboutWorkaround() throws MigrationException {
        Trigger trigger = Trigger.builder()
                .type(TriggerType.FILE_WATCH)
                .jobName("Job")
                .watchPath("/data/file.csv")
                .build();

        RedwoodJobDto result = handler.migrate(trigger);

        assertFalse(result.getNotes().isEmpty());
        assertTrue(result.getNotes().stream()
                .anyMatch(n -> n.contains("WORKAROUND")));
    }

    @Test
    void migrate_shouldThrowException_whenWatchPathIsMissing() {
        Trigger trigger = Trigger.builder()
                .type(TriggerType.FILE_WATCH)
                .jobName("Job")
                .build();

        assertThrows(MigrationException.class, () -> handler.migrate(trigger));
    }
}

