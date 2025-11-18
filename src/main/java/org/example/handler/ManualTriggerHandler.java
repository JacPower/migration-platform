package org.example.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.internal.Trigger;
import org.example.dto.internal.ValidationResult;
import org.example.dto.output.RedwoodJobDto;
import org.example.dto.output.RedwoodTriggerDto;
import org.example.service.TriggerHandler;
import org.example.service.TriggerType;


@Slf4j
public class ManualTriggerHandler implements TriggerHandler {

    @Override
    public TriggerType getSupportedType() {
        return TriggerType.MANUAL;
    }



    @Override
    public boolean canHandle(Trigger trigger) {
        return trigger.getType() == TriggerType.MANUAL;
    }



    @Override
    public ValidationResult validate(Trigger trigger) {
        return new ValidationResult(); // No special validation needed
    }



    @Override
    public RedwoodJobDto migrate(Trigger trigger) {
        log.info("Migrating MANUAL trigger for job: {}", trigger.getJobName());

        RedwoodTriggerDto redwoodTrigger = RedwoodTriggerDto.builder()
                .type("MANUAL")
                .apiEnabled(true)
                .build();

        return RedwoodJobDto.builder()
                .name(trigger.getJobName())
                .type("MANUAL")
                .trigger(redwoodTrigger)
                .build();
    }



    @Override
    public String getDescription() {
        return "Direct migration - RMJ supports manual triggers via API";
    }
}
