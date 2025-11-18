package org.example.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.internal.Trigger;
import org.example.dto.internal.ValidationResult;
import org.example.service.TriggerHandler;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
@Builder
public class MigrationAnalysis {

    @Builder.Default
    private List<SupportedTrigger> supported = new ArrayList<>();

    @Builder.Default
    private List<WorkaroundTrigger> workarounds = new ArrayList<>();

    @Builder.Default
    private List<UnsupportedTrigger> unsupported = new ArrayList<>();

    @Builder.Default
    private List<InvalidTrigger> invalid = new ArrayList<>();



    public void addSupported(Trigger trigger, TriggerHandler handler) {
        supported.add(new SupportedTrigger(trigger, handler));
    }



    public void addWorkaround(Trigger trigger, TriggerHandler handler, ValidationResult validation) {
        workarounds.add(new WorkaroundTrigger(trigger, handler, validation));
    }



    public void addUnsupported(Trigger trigger) {
        unsupported.add(new UnsupportedTrigger(trigger));
    }



    public void addInvalid(Trigger trigger, ValidationResult validation) {
        invalid.add(new InvalidTrigger(trigger, validation));
    }



    public int getTotalCount() {
        return supported.size() + workarounds.size() + unsupported.size() + invalid.size();
    }



    public void printReport() {
        log.info("=== TRIGGER MIGRATION ANALYSIS ===");
        log.info("Total Triggers: {}", getTotalCount());
        log.info("Direct Migration: {}", supported.size());
        log.info(" With Workarounds: {}", workarounds.size());
        log.info("Cannot Migrate: {}", unsupported.size() + invalid.size());

        if (!supported.isEmpty()) logSupported();
        if (!workarounds.isEmpty()) logWorkarounds();
        if (!unsupported.isEmpty()) logUnsupported();
        if (!invalid.isEmpty()) logInvalid();

        logRecommendation();
    }


    // --------------------- Section Loggers ---------------------



    private void logSupported() {
        log.info("\nDIRECT MIGRATION:");
        supported.forEach(st -> {
            log.info("  - {}", st.getTrigger().getJobName());
            log.info("    Type: {}", st.getTrigger().getType());
            log.info("    Strategy: {}", st.getHandler().getDescription());
        });
    }



    private void logWorkarounds() {
        log.warn("\nMIGRATION WITH WORKAROUNDS:");
        workarounds.forEach(wt -> {
            log.warn("  - {}", wt.getTrigger().getJobName());
            log.warn("    Type: {}", wt.getTrigger().getType());
            log.warn("    Strategy: {}", wt.getHandler().getDescription());
            wt.getValidation().getWarnings()
                    .forEach(w -> log.warn("    Warning: {}", w));
        });
    }



    private void logUnsupported() {
        log.error("\n❌ UNSUPPORTED TRIGGERS:");
        unsupported.forEach(ut -> {
            log.error("  - {}", ut.getTrigger().getJobName());
            log.error("    Type: {}", ut.getTrigger().getType());
            log.error("    Action: Manual configuration required");
        });
    }



    private void logInvalid() {
        log.error("\n❌ INVALID TRIGGERS:");
        invalid.forEach(it -> {
            log.error("  - {}", it.getTrigger().getJobName());
            log.error("    Type: {}", it.getTrigger().getType());
            it.getValidation().getErrors()
                    .forEach(e -> log.error("    Error: {}", e));
        });
    }



    private void logRecommendation() {
        boolean allGood = unsupported.isEmpty() && invalid.isEmpty();

        log.info("\nRECOMMENDATION:");
        if (allGood) {
            log.info("  All triggers can be migrated!");
        } else {
            int blocked = unsupported.size() + invalid.size();
            log.warn("  {} triggers require manual attention.", blocked);
        }
    }



    @Data
    @AllArgsConstructor
    public static class SupportedTrigger {
        private Trigger trigger;
        private TriggerHandler handler;
    }

    @Data
    @AllArgsConstructor
    public static class WorkaroundTrigger {
        private Trigger trigger;
        private TriggerHandler handler;
        private ValidationResult validation;
    }

    @Data
    @AllArgsConstructor
    public static class UnsupportedTrigger {
        private Trigger trigger;
    }

    @Data
    @AllArgsConstructor
    public static class InvalidTrigger {
        private Trigger trigger;
        private ValidationResult validation;
    }
}
