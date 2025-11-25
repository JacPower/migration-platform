package unit.handler;

import org.example.dto.internal.Trigger;
import org.example.dto.internal.ValidationResult;
import org.example.dto.output.RedwoodJobDto;
import org.example.handler.ManualTriggerHandler;
import org.example.service.TriggerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ManualTriggerHandler Tests")
class ManualTriggerHandlerTest {

    private ManualTriggerHandler handler;



    @BeforeEach
    void setUp() {
        handler = new ManualTriggerHandler();
    }



    @Test
    void getSupportedType_shouldReturnManualType() {
        assertEquals(TriggerType.MANUAL, handler.getSupportedType());
    }



    @Test
    void canHandle_shouldReturnTrue_forManualTrigger() {
        Trigger trigger = Trigger.builder().type(TriggerType.MANUAL).build();
        assertTrue(handler.canHandle(trigger));
    }



    @Test
    void validate_shouldAlwaysPass_forManualTrigger() {
        Trigger trigger = Trigger.builder().type(TriggerType.MANUAL).build();
        ValidationResult result = handler.validate(trigger);
        assertTrue(result.isValid());
    }



    @Test
    void migrate_shouldReturnRedwoodJob_withApiEnabled() {
        Trigger trigger = Trigger.builder()
                .type(TriggerType.MANUAL)
                .jobName("Manual_Job")
                .build();

        RedwoodJobDto result = handler.migrate(trigger);

        assertNotNull(result);
        assertEquals("Manual_Job", result.getName());
        assertEquals("MANUAL", result.getType());
        assertTrue(result.getTrigger().getApiEnabled());
    }
}

