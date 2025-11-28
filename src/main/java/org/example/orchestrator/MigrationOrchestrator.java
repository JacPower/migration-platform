package org.example.orchestrator;

import lombok.extern.slf4j.Slf4j;
import org.example.config.MigrationDependencies;
import org.example.dto.input.ExportDataDto;
import org.example.dto.input.JobDto;
import org.example.dto.input.TriggerDto;
import org.example.dto.internal.Trigger;
import org.example.dto.internal.ValidationResult;
import org.example.exception.ValidationException;
import org.example.parser.BatchFileParser;
import org.example.parser.DataParser;
import org.example.report.MigrationAnalysis;
import org.example.report.MigrationResult;
import org.example.service.TriggerMigrationService;
import org.example.validator.Validator;

import java.io.IOException;
import java.util.List;

@Slf4j
public class MigrationOrchestrator  implements AutoCloseable {

    private static final String MIGRATION_FAILED = "Migration failed";
    private static final String ORCHESTRATOR_SHUTDOWN = "Shutting down orchestrator...";
    private static final String SHUTDOWN_COMPLETE = "Shutdown complete";

    private final DataParser dataParser;
    private final BatchFileParser batchParser;
    private final Validator validator;
    private final TriggerMigrationService triggerService;
    private final String outputPath;



    public MigrationOrchestrator(MigrationDependencies dependencies, String outputPath) {
        this.dataParser = dependencies.dataParser();
        this.batchParser = dependencies.batchFileParser();
        this.validator = dependencies.validator();
        this.triggerService = dependencies.triggerService();
        this.outputPath = outputPath;
        log.info("Migration orchestrator initialized");
    }



    public MigrationOrchestrator(String outputPath) {
        this(MigrationDependencies.createDefault(), outputPath);
    }



    public void migrate(List<String> filePaths) throws IOException {
        if(filePaths.size() == 1) {
            long start = System.currentTimeMillis();
            log.info("Starting migration from file: {}", filePaths.get(0));

            ExportDataDto export = dataParser.parse(filePaths.get(0));
            log.info("Parsed {} jobs from export file", export.getJobs().size());

            validateOrThrow(export);

            List<Trigger> triggers = convertToTriggers(export.getJobs());

            analyzeTriggers(triggers);

            log.info("Migrating triggers to Redwood...");
            MigrationResult result = triggerService.migrateAll(triggers);

            logCompletion(result, start);
        } else {
            migrateAsync(filePaths);
        }
    }



    private void migrateAsync(List<String> paths) {
        long start = System.currentTimeMillis();
        log.info("Starting concurrent migration for {} files", paths.size());

        batchParser.parseMultipleFiles(paths)
                .thenApply(this::validateAndReturnJobs)
                .thenApply(this::analyzeAndMigrate)
                .thenApply(result -> logCompletion(result, start))
                .exceptionally(e -> {
                    log.error(MIGRATION_FAILED, e);
                    return MigrationResult.builder().build();
                });
    }



    private void validateOrThrow(ExportDataDto exportDataDto) {
        ValidationResult validation = validator.validate(exportDataDto);
        log.info("Validation result:\n{}", validation);

        if (!validation.isValid()) {
            log.error("Validation failed. Migration aborted.");
            throw new ValidationException("Validation failed:\n" + validation);
        }

        log.info("Validation passed");
    }



    private List<JobDto> validateAndReturnJobs(List<JobDto> jobs) {
        log.info("Parsed {} jobs list", jobs.size());
        ExportDataDto export = ExportDataDto.builder().jobs(jobs).build();
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
                .outputFolderPath(outputPath)
                .build();
    }



    @Override
    public void close() {
        log.info(ORCHESTRATOR_SHUTDOWN);
        batchParser.shutdown();
        validator.shutdown();
        log.info(SHUTDOWN_COMPLETE);
    }
}
