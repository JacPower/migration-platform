package org.example.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.internal.Trigger;
import org.example.dto.internal.ValidationResult;
import org.example.dto.output.RedwoodJobDto;
import org.example.dto.output.RedwoodTriggerDto;
import org.example.exception.MigrationException;
import org.example.service.TriggerHandler;
import org.example.service.TriggerType;
import org.example.utils.Constants;
import org.example.utils.FileUtils;

import java.util.Date;

@Slf4j
public class FileWatchTriggerHandler implements TriggerHandler {

    private static final String POLLING_SCHEDULE = "*/5 * * * *"; // Poll every 5 minutes

    @Override
    public TriggerType getSupportedType() {
        return TriggerType.FILE_WATCH;
    }

    @Override
    public boolean canHandle(Trigger trigger) {
        return trigger.getType() == TriggerType.FILE_WATCH;
    }

    @Override
    public ValidationResult validate(Trigger trigger) {
        ValidationResult result = new ValidationResult();

        validateWatchPath(trigger, result);
        addPollingWarning(result);

        return result;
    }

    private void validateWatchPath(Trigger trigger, ValidationResult result) {
        if (trigger.getWatchPath() == null || trigger.getWatchPath().isEmpty()) {
            result.addError("File watch trigger missing watch path");
        }
    }

    private void addPollingWarning(ValidationResult result) {
        result.addWarning(
                "File watch triggers will be converted to 5-minute polling. " +
                "Real-time file detection not available."
        );
    }

    @Override
    public RedwoodJobDto migrate(Trigger trigger) throws MigrationException {
        validateTrigger(trigger);

        logConversion(trigger);

        RedwoodTriggerDto redwoodTrigger = buildRedwoodTrigger(trigger);
        var redwoodJobDto = buildRedwoodJob(trigger, redwoodTrigger);

        addMigrationNotes(redwoodJobDto, trigger);

        String outputFileName = redwoodJobDto.getName() + "_" + new Date().getTime() + ".json";
        String outputPath = trigger.getOutputFolderPath();
        FileUtils.writeToJsonFile(redwoodJobDto, outputFileName, outputPath);

        return redwoodJobDto;
    }

    private void validateTrigger(Trigger trigger) throws MigrationException {
        ValidationResult validation = validate(trigger);
        if (!validation.isValid()) {
            throw new MigrationException("Validation failed: " + validation.getErrors().get(0));
        }
    }

    private void logConversion(Trigger trigger) {
        log.warn("FILE_WATCH trigger converted to polling for job: {}", trigger.getJobName());
    }

    private RedwoodTriggerDto buildRedwoodTrigger(Trigger trigger) {
        return RedwoodTriggerDto.builder()
                .type("SCHEDULED")
                .schedule(POLLING_SCHEDULE)
                .timezone("UTC")
                .preScript(buildCheckScript(trigger.getWatchPath()))
                .build();
    }

    private String buildCheckScript(String watchPath) {
        return String.format(
                """
                #!/bin/bash
                # Check if file exists before running job
                if [ ! -f %s ]; then
                 echo 'File not found, skipping execution'
                 exit 1
                fi
                """, watchPath
        );
    }

    private RedwoodJobDto buildRedwoodJob(Trigger trigger, RedwoodTriggerDto redwoodTrigger) {
        return RedwoodJobDto.builder()
                .name(trigger.getJobName())
                .type("SCHEDULED")
                .trigger(redwoodTrigger)
                .build();
    }

    private void addMigrationNotes(RedwoodJobDto job, Trigger trigger) {
        job.addNote("WORKAROUND: File watch converted to 5-minute polling");
        job.addNote("Original watch path: " + trigger.getWatchPath());
        job.addMetadata("original_trigger_type", "FILE_WATCH");
        job.addMetadata("migration_strategy", "POLLING_WORKAROUND");
    }

    @Override
    public String getDescription() {
        return "Workaround - File watch converted to scheduled polling (5 min interval)";
    }
}
