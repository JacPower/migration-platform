package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.internal.Trigger;
import org.example.dto.internal.ValidationResult;
import org.example.dto.output.RedwoodJobDto;
import org.example.exception.MigrationException;
import org.example.report.MigrationAnalysis;
import org.example.report.MigrationResult;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class TriggerMigrationService {

    private static final String HANDLER_PACKAGE = "org.example.handler";

    private final List<TriggerHandler> handlers;



    // Constructor injection for testing
    public TriggerMigrationService(List<TriggerHandler> handlers) {
        this.handlers = new ArrayList<>(handlers);
        log.info("Initialized with {} handlers", this.handlers.size());
    }



    public TriggerMigrationService() {
        this.handlers = new ArrayList<>();
        registerHandlers();
    }



    private void registerHandlers() {
        registerHandlersUsingReflection();

        if (handlers.isEmpty()) {
            throw new IllegalStateException("No TriggerHandlers found in package: " + HANDLER_PACKAGE);
        }
    }



    /*The Reflection API is used to manage dependency injection, as the application does not provide its own IoC container.*/
    private void registerHandlersUsingReflection() {
        Reflections reflections = new Reflections(HANDLER_PACKAGE);
        Set<Class<? extends TriggerHandler>> handlerClasses = reflections.getSubTypesOf(TriggerHandler.class);

        for (Class<? extends TriggerHandler> handlerClass : handlerClasses) {
            instantiateAndRegister(handlerClass);
        }

        log.info("Auto-registered {} handlers via reflection", handlers.size());
    }



    private void instantiateAndRegister(Class<? extends TriggerHandler> handlerClass) {
        try {
            TriggerHandler handler = handlerClass.getDeclaredConstructor().newInstance();
            handlers.add(handler);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error("Failed to register handler {}:", handlerClass.getName(), e);
        }
    }



    public MigrationAnalysis analyze(List<Trigger> triggers) {
        log.info("Analyzing {} triggers", triggers.size());
        MigrationAnalysis analysis = MigrationAnalysis.builder().build();

        for (Trigger trigger : triggers) {
            analyzeTrigger(trigger, analysis);
        }

        return analysis;
    }



    private void analyzeTrigger(Trigger trigger, MigrationAnalysis analysis) {
        Optional<TriggerHandler> handlerOpt = findHandler(trigger);

        if (handlerOpt.isEmpty()) {
            analysis.addUnsupported(trigger);
            return;
        }

        TriggerHandler handler = handlerOpt.get();
        ValidationResult validation = handler.validate(trigger);

        if (validation.isValid()) {
            analysis.addSupported(trigger, handler);
        } else if (!validation.hasErrors()) {
            analysis.addWorkaround(trigger, handler, validation);
        } else {
            analysis.addInvalid(trigger, validation);
        }
    }



    public RedwoodJobDto migrate(Trigger trigger) throws MigrationException {
        TriggerHandler handler = findHandler(trigger)
                .orElseThrow(() -> new MigrationException("No handler for trigger type: " + trigger.getType()));

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
            migrateSingle(trigger, result);
        }

        return result;
    }



    private void migrateSingle(Trigger trigger, MigrationResult result) {
        try {
            RedwoodJobDto job = migrate(trigger);
            result.addSuccess(trigger, job);
        } catch (MigrationException e) {
            result.addFailure(trigger, e.getMessage());
            log.error("Failed to migrate trigger {}: {}", trigger.getJobName(), e.getMessage());
        }
    }



    private Optional<TriggerHandler> findHandler(Trigger trigger) {
        return handlers.stream()
                .filter(handler -> handler.canHandle(trigger))
                .findFirst();
    }
}

