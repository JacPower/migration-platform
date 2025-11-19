package org.example.validator;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.input.CompetitorExportDto;
import org.example.dto.input.JobDto;
import org.example.dto.internal.ConcurrentValidationResult;
import org.example.dto.internal.ValidationResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Slf4j
public class ConcurrentJobValidator implements ExportValidator {

    private final ExecutorService executor;
    private final JobValidator sequentialValidator;



    public ConcurrentJobValidator() {
        int threads = Runtime.getRuntime().availableProcessors();
        this.executor = Executors.newFixedThreadPool(threads);
        this.sequentialValidator = new JobValidator();
        log.info("Initialized concurrent validator with {} threads", threads);
    }



    @Override
    public ValidationResult validate(CompetitorExportDto export) {
        List<JobDto> jobs = export.getJobs();

        if (jobs.size() < 10) {
            log.info("Small batch ({}), using sequential validation", jobs.size());
            return sequentialValidator.validate(export);
        }

        log.info("Large batch ({}), using concurrent validation", jobs.size());
        return validateConcurrent(export);
    }



    private ValidationResult validateConcurrent(CompetitorExportDto export) {
        ConcurrentValidationResult result = new ConcurrentValidationResult();
        List<JobDto> jobs = export.getJobs();


        CompletableFuture.allOf(jobs.stream() // Wait for all validations to complete
                .map(job -> CompletableFuture.runAsync(() -> validateJob(job, result), executor))
                .toArray(CompletableFuture[]::new)).join();


        ValidationResult finalResult = result.toValidationResult(); // Now validate dependencies (requires all jobs)
        sequentialValidator.validate(export).getErrors()
                .forEach(finalResult::addError);

        return finalResult;
    }



    private void validateJob(JobDto job, ConcurrentValidationResult result) {
        if (job.getJobName() == null || job.getJobName().isEmpty()) {
            result.addError(String.format("Job %d has no name", job.getJobId()));
        }

        if (job.getTrigger() == null) {
            result.addError(String.format("Job %d (%s) has no trigger", job.getJobId(), job.getJobName()));
        }
    }



    @Override
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
