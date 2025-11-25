package unit.handler;

import org.example.dto.internal.Trigger;
import org.example.dto.internal.ValidationResult;
import org.example.dto.output.RedwoodJobDto;
import org.example.exception.MigrationException;
import org.example.handler.ScheduleTriggerHandler;
import org.example.service.TriggerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ScheduleTriggerHandler Tests")
class ScheduleTriggerHandlerTest {

    private ScheduleTriggerHandler handler;



    @BeforeEach
    void setUp() {
        handler = new ScheduleTriggerHandler();
    }



    @Test
    void getSupportedType_shouldReturnScheduleType() {
        assertEquals(TriggerType.SCHEDULE, handler.getSupportedType());
    }



    @Test
    void canHandle_shouldReturnTrue_forScheduleTrigger() {
        Trigger trigger = Trigger.builder().type(TriggerType.SCHEDULE).build();
        assertTrue(handler.canHandle(trigger));
    }



    @Test
    void canHandle_shouldReturnFalse_forManualTrigger() {
        Trigger trigger = Trigger.builder().type(TriggerType.MANUAL).build();
        assertFalse(handler.canHandle(trigger));
    }



    @Test
    void canHandle_shouldReturnFalse_whenTriggerTypeIsNull() {
        Trigger trigger = Trigger.builder().build();
        assertFalse(handler.canHandle(trigger));
    }



    @Test
    void validate_shouldPass_forValidScheduleTrigger() {
        Trigger trigger = Trigger.builder()
                .type(TriggerType.SCHEDULE)
                .cronExpression("0 2 * * *")
                .build();

        ValidationResult result = handler.validate(trigger);

        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
    }



    @Test
    void validate_shouldFail_whenCronIsNull() {
        Trigger trigger = Trigger.builder()
                .type(TriggerType.SCHEDULE)
                .cronExpression(null)
                .build();

        ValidationResult result = handler.validate(trigger);

        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
    }



    @Test
    void validate_shouldFail_whenCronIsEmpty() {
        Trigger trigger = Trigger.builder()
                .type(TriggerType.SCHEDULE)
                .cronExpression("")
                .build();

        ValidationResult result = handler.validate(trigger);

        assertFalse(result.isValid());
    }



    @Test
    void validate_shouldFail_forInvalidCronExpression() {
        Trigger trigger = Trigger.builder()
                .type(TriggerType.SCHEDULE)
                .cronExpression("invalid")
                .build();

        ValidationResult result = handler.validate(trigger);

        assertFalse(result.isValid());
    }



    @Test
    void migrate_shouldReturnRedwoodJob_forValidTrigger() throws MigrationException {
        Trigger trigger = Trigger.builder()
                .type(TriggerType.SCHEDULE)
                .jobName("Daily_Backup")
                .cronExpression("0 2 * * *")
                .timezone("UTC")
                .build();

        RedwoodJobDto result = handler.migrate(trigger);

        assertNotNull(result);
        assertEquals("Daily_Backup", result.getName());
        assertEquals("SCHEDULED", result.getType());
        assertNotNull(result.getTrigger());
        assertEquals("0 2 * * *", result.getTrigger().getSchedule());
        assertEquals("UTC", result.getTrigger().getTimezone());
    }



    @Test
    void migrate_shouldDefaultToUtc_whenTimezoneNotProvided() throws MigrationException {
        Trigger trigger = Trigger.builder()
                .type(TriggerType.SCHEDULE)
                .jobName("Job")
                .cronExpression("0 2 * * *")
                .build();

        RedwoodJobDto result = handler.migrate(trigger);

        assertEquals("UTC", result.getTrigger().getTimezone());
    }



    @Test
    void migrate_shouldThrowException_forInvalidTrigger() {
        Trigger trigger = Trigger.builder()
                .type(TriggerType.SCHEDULE)
                .jobName("Job")
                .build();

        assertThrows(MigrationException.class, () -> handler.migrate(trigger));
    }



    @Test
    void getDescription_shouldContainDirectMigration() {
        String description = handler.getDescription();
        assertNotNull(description);
        assertTrue(description.contains("Direct migration"));
    }
}

