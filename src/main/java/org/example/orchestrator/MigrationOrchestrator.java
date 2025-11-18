package org.example.orchestrator;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.input.CompetitorExportDto;
import org.example.dto.input.JobDto;
import org.example.dto.input.TriggerDto;
import org.example.dto.internal.Trigger;
import org.example.dto.internal.ValidationResult;
import org.example.exception.ValidationException;
import org.example.parser.CompetitorDataParser;
import org.example.parser.ConcurrentFileParser;
import org.example.report.MigrationAnalysis;
import org.example.report.MigrationResult;
import org.example.service.TriggerMigrationService;
import org.example.service.TriggerType;
import org.example.validator.ConcurrentJobValidator;

import java.io.IOException;
import java.util.List;

@Slf4j
public class MigrationOrchestrator {

    private static final String MIGRATION_FAILED = "Migration failed";
    private static final String ORCHESTRATOR_SHUTDOWN = "Shutting down orchestrator...";
    private static final String SHUTDOWN_COMPLETE = "Shutdown complete";
    private static final String UNKNOWN_TRIGGER_WARN = "Unknown trigger type '{}', using UNKNOWN";

    private final CompetitorDataParser competitorParser;
    private final ConcurrentFileParser fileParser;
    private final ConcurrentJobValidator jobValidator;
    private final TriggerMigrationService triggerService;



    public MigrationOrchestrator() {
        this.competitorParser = new CompetitorDataParser();
        this.fileParser = new ConcurrentFileParser();
        this.jobValidator = new ConcurrentJobValidator();
        this.triggerService = new TriggerMigrationService();
        log.info("Migration orchestrator initialized");
    }



    public void migrate(String filePath) throws IOException {
        long start = System.currentTimeMillis();
        log.info("Starting migration from file: {}", filePath);

        CompetitorExportDto export = competitorParser.parseJson(filePath);
        log.info("Parsed {} jobs from export file", export.getJobs().size());

        validateOrThrow(export);

        List<Trigger> triggers = convertToTriggers(export.getJobs());

        analyzeTriggers(triggers);

        log.info("Migrating triggers to Redwood...");
        MigrationResult result = triggerService.migrateAll(triggers);

        logCompletion(result, start);
    }



    public void migrateAsync(List<String> paths) {
        long start = System.currentTimeMillis();
        log.info("Starting concurrent migration for {} files", paths.size());

        fileParser.parseMultipleFiles(paths)
                .thenApply(this::validateAndReturnJobs)
                .thenApply(this::analyzeAndMigrate)
                .thenApply(result -> logCompletion(result, start))
                .exceptionally(e -> {
                    log.error(MIGRATION_FAILED, e);
                    return MigrationResult.builder().build();
                });
    }



    private void validateOrThrow(CompetitorExportDto export) {
        ValidationResult validation = jobValidator.validate(export);
        log.info("Validation result:\n{}", validation);
        if (!validation.isValid()) {
            log.error("Validation failed. Migration aborted.");
            throw new ValidationException("Validation failed:\n" + validation);
        }
        log.info("Validation passed");
    }



    private List<JobDto> validateAndReturnJobs(List<JobDto> jobs) {
        log.info("Parsed {} jobs list", jobs.size());
        CompetitorExportDto export = CompetitorExportDto.builder().jobs(jobs).build();
        validateOrThrow(export);
        return jobs;
    }



    private void analyzeTriggers(List<Trigger> triggers) {
        log.info("Analyzing and migrating triggers...");
        MigrationAnalysis analysis = triggerService.analyze(triggers);
        analysis.printReport();
    }



    private MigrationResult analyzeAndMigrate(List<JobDto> jobs) {
        List<Trigger> triggers = convertToTriggers(jobs);
        analyzeTriggers(triggers);
        return triggerService.migrateAll(triggers);
    }



    private MigrationResult logCompletion(MigrationResult result, long start) {
        log.info("Migration completed in {} ms", System.currentTimeMillis() - start);
        result.printReport();
        return result;
    }



    private List<Trigger> convertToTriggers(List<JobDto> jobs) {
        return jobs.stream()
                .map(this::convertToTrigger)
                .toList();
    }



    private Trigger convertToTrigger(JobDto job) {
        TriggerDto dto = job.getTrigger();
        return Trigger.builder()
                .type(dto.getType())
                .jobName(job.getJobName())
                .cronExpression(dto.getCronExpression())
                .timezone(dto.getTimezone())
                .watchPath(dto.getWatchPath())
                .filePattern(dto.getFilePattern())
                .eventSource(dto.getEventSource())
                .eventType(dto.getEventType())
                .upstreamJobId(dto.getUpstreamJobId())
                .build();
    }



    private TriggerType parseTriggerType(String type) {
        try {
            return TriggerType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn(UNKNOWN_TRIGGER_WARN, type);
            return TriggerType.UNKNOWN;
        }
    }



    public void shutdown() {
        log.info(ORCHESTRATOR_SHUTDOWN);
        fileParser.shutdown();
        jobValidator.shutdown();
        log.info(SHUTDOWN_COMPLETE);
    }
}
