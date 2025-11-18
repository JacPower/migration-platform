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
public class DependencyTriggerHandler implements TriggerHandler {

    @Override
    public TriggerType getSupportedType() {
        return TriggerType.DEPENDENCY;
    }



    @Override
    public boolean canHandle(Trigger trigger) {
        return trigger.getType() == TriggerType.DEPENDENCY;
    }



    @Override
    public ValidationResult validate(Trigger trigger) {
        ValidationResult result = new ValidationResult();

        if (trigger.getUpstreamJobId() == null) {
            result.addError("Dependency trigger missing upstream job ID");
        }

        if (trigger.getUpstreamJobId() != null && trigger.getUpstreamJobId() <= 0) {
            result.addError("Upstream job ID must be positive: " + trigger.getUpstreamJobId());
        }

        return result;
    }



    @Override
    public RedwoodJobDto migrate(Trigger trigger) throws MigrationException {
        ValidationResult validation = validate(trigger);
        if (!validation.isValid()) {
            throw new MigrationException("Validation failed: " + validation.getErrors().get(0));
        }

        log.info("Migrating DEPENDENCY trigger for job: {} (depends on job {})",
                trigger.getJobName(), trigger.getUpstreamJobId());

        RedwoodTriggerDto redwoodTrigger = RedwoodTriggerDto.builder()
                .type("EVENT")
                .apiEnabled(false)
                .build();

        RedwoodJobDto job = RedwoodJobDto.builder()
                .name(trigger.getJobName())
                .type("DEPENDENCY")
                .trigger(redwoodTrigger)
                .build();

        job.addMetadata("upstream_job_id", String.valueOf(trigger.getUpstreamJobId()));
        job.addMetadata("trigger_type", "DEPENDENCY");
        job.addMetadata("trigger_condition", "ON_SUCCESS");

        job.addNote(String.format("This job is triggered when job %d completes successfully", trigger.getUpstreamJobId()));

        return job;
    }



    @Override
    public String getDescription() {
        return "Direct migration - RMJ supports dependency-based job chaining";
    }
}
