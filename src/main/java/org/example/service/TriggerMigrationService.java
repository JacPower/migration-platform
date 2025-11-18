package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.internal.Trigger;
import org.example.dto.internal.ValidationResult;
import org.example.dto.output.RedwoodJobDto;
import org.example.exception.MigrationException;
import org.example.handler.*;
import org.example.report.MigrationAnalysis;
import org.example.report.MigrationResult;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TriggerMigrationService {

    private final List<TriggerHandler> handlers;



    public TriggerMigrationService() {
        this.handlers = new ArrayList<>();
        registerDefaultHandlers();
    }



    /*
     * If using frameworks like Springboot: Auto-detect all beans implementing TriggerHandler.
     * Reflection can also be used to find all classes implementing TriggerHandler interface.
     * For simplicity, we manually register default handlers here.
     */
    private void registerDefaultHandlers() {
        handlers.add(new ScheduleTriggerHandler());
        handlers.add(new ManualTriggerHandler());
        handlers.add(new ApiTriggerHandler());
        handlers.add(new FileWatchTriggerHandler());
        handlers.add(new DependencyTriggerHandler());
        log.info("Registered {} trigger handlers", handlers.size());
    }



    public MigrationAnalysis analyze(List<Trigger> triggers) {
        log.info("Analyzing {} triggers", triggers.size());
        MigrationAnalysis analysis = MigrationAnalysis.builder().build();

        for (Trigger trigger : triggers) {
            TriggerHandler handler = findHandler(trigger);

            if (handler != null) {
                ValidationResult validation = handler.validate(trigger);
                if (validation.isValid()) {
                    analysis.addSupported(trigger, handler);
                } else if (!validation.hasErrors()) {
                    analysis.addWorkaround(trigger, handler, validation);
                } else {
                    analysis.addInvalid(trigger, validation);
                }
            } else {
                analysis.addUnsupported(trigger);
            }
        }

        return analysis;
    }



    public RedwoodJobDto migrate(Trigger trigger) throws MigrationException {
        TriggerHandler handler = findHandler(trigger);

        if (handler == null) {
            throw new MigrationException("No handler available for trigger type: " + trigger.getType());
        }

        ValidationResult validation = handler.validate(trigger);
        if (!validation.isValid()) {
            throw new MigrationException("Validation failed: " + validation.getErrors().get(0));
        }

        return handler.migrate(trigger);
    }



    public MigrationResult migrateAll(List<Trigger> triggers) {
        log.info("Migrating {} triggers", triggers.size());
        MigrationResult result = MigrationResult.builder().build();

        for (Trigger trigger : triggers) {
            try {
                RedwoodJobDto job = migrate(trigger);
                result.addSuccess(trigger, job);
            } catch (MigrationException e) {
                result.addFailure(trigger, e.getMessage());
                log.error("Failed to migrate trigger {}: {}", trigger.getJobName(), e.getMessage());
            }
        }

        return result;
    }



    private TriggerHandler findHandler(Trigger trigger) {
        return handlers.stream()
                .filter(triggerHandler -> triggerHandler.canHandle(trigger))
                .findFirst()
                .orElse(null);
    }
}

