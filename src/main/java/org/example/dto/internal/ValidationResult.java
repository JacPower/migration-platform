package org.example.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationResult {

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();



    public void addError(String message) {
        errors.add(message);
    }



    public void addWarning(String message) {
        warnings.add(message);
    }



    public boolean hasErrors() {
        return !errors.isEmpty();
    }



    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }



    public boolean isValid() {
        return errors.isEmpty();
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("=== VALIDATION REPORT ===%n%n"));

        if (errors.isEmpty() && warnings.isEmpty()) {
            sb.append(String.format("All validations passed!%n"));
            return sb.toString();
        }

        appendSection(sb, "ERRORS", errors);
        appendSection(sb, "WARNINGS", warnings);

        sb.append(resultSummary());

        return sb.toString();
    }



    private void appendSection(StringBuilder sb, String title, List<String> items) {
        if (items == null || items.isEmpty()) return;

        sb.append(String.format("%s (%d):%n", title, items.size()));

        for (int i = 0; i < items.size(); i++) {
            sb.append(String.format("  %d. %s%n", i + 1, items.get(i)));
        }

        sb.append(String.format("%n"));
    }



    private String resultSummary() {
        return hasErrors()
                ? String.format("Validation FAILED. Fix errors before proceeding.%n")
                : String.format("Validation PASSED with warnings.%n");
    }

}
