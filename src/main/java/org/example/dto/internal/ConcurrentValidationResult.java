package org.example.dto.internal;

import lombok.Data;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

@Data
public class ConcurrentValidationResult {

    private final ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<String> warnings = new ConcurrentLinkedQueue<>();



    public void addError(String message) {
        errors.add(message);
    }



    public void addWarning(String message) {
        warnings.add(message);
    }



    public ValidationResult toValidationResult() {
        ValidationResult result = new ValidationResult();
        result.setErrors(new ArrayList<>(errors));
        result.setWarnings(new ArrayList<>(warnings));
        return result;
    }
}
