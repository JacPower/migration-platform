package org.example.report;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.internal.Trigger;
import org.example.dto.output.RedwoodJobDto;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Data
@Builder
public class MigrationResult {

    @Builder.Default
    private Map<String, String> successes = new HashMap<>();

    @Builder.Default
    private Map<String, String> failures = new HashMap<>();



    public void addSuccess(Trigger trigger, RedwoodJobDto job) {
        successes.put(trigger.getJobName(), "Migrated successfully");
    }



    public void addFailure(Trigger trigger, String reason) {
        failures.put(trigger.getJobName(), reason);
    }



    public int getSuccessCount() {
        return successes.size();
    }



    public int getFailureCount() {
        return failures.size();
    }



    public boolean hasFailures() {
        return !failures.isEmpty();
    }



    public void printReport() {
        log.info("\n=== MIGRATION RESULT ===");

        int total = successes.size() + failures.size();
        log.info("Total: {}", total);
        log.info("Success: {}", successes.size());
        log.info("Failed: {}", failures.size());

        if (!failures.isEmpty()) {
            log.error("\nFailures:");
            failures.forEach((job, reason) ->
                    log.error("  - {}: {}", job, reason)
            );
        }
    }
}
