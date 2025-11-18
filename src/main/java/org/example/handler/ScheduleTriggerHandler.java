package org.example.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.internal.Trigger;
import org.example.dto.internal.ValidationResult;
import org.example.dto.output.RedwoodJobDto;
import org.example.dto.output.RedwoodTriggerDto;
import org.example.exception.MigrationException;
import org.example.service.TriggerHandler;
import org.example.service.TriggerType;


@Slf4j
public class ScheduleTriggerHandler implements TriggerHandler {

    @Override
    public TriggerType getSupportedType() {
        return TriggerType.SCHEDULE;
    }



    @Override
    public boolean canHandle(Trigger trigger) {
        return trigger.getType() == TriggerType.SCHEDULE;
    }



    @Override
    public ValidationResult validate(Trigger trigger) {
        ValidationResult result = new ValidationResult();

        if (trigger.getCronExpression() == null || trigger.getCronExpression().isEmpty()) {
            result.addError("Schedule trigger missing cron expression");
        } else if (!isValidCronExpression(trigger.getCronExpression())) {
            result.addError("Invalid cron expression: " + trigger.getCronExpression());
        }

        return result;
    }



    @Override
    public RedwoodJobDto migrate(Trigger trigger) throws MigrationException {
        ValidationResult validation = validate(trigger);
        if (!validation.isValid()) {
            throw new MigrationException("Validation failed: " + validation.getErrors().get(0));
        }

        log.info("Migrating SCHEDULE trigger for job: {}", trigger.getJobName());

        RedwoodTriggerDto redwoodTrigger = RedwoodTriggerDto.builder()
                .type("SCHEDULED")
                .schedule(trigger.getCronExpression())
                .timezone(trigger.getTimezone() != null ? trigger.getTimezone() : "UTC")
                .build();

        return RedwoodJobDto.builder()
                .name(trigger.getJobName())
                .type("SCHEDULED")
                .trigger(redwoodTrigger)
                .build();
    }



    @Override
    public String getDescription() {
        return "Direct migration - RMJ fully supports scheduled triggers";
    }



    private boolean isValidCronExpression(String cron) {
        String[] parts = cron.trim().split("\\s+");
        return parts.length >= 5 && parts.length <= 6;
    }
}
