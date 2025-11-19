package org.example.validator;

import org.example.dto.input.ExportDataDto;
import org.example.dto.internal.ValidationResult;

public interface Validator {
    ValidationResult validate(ExportDataDto export);

    void shutdown();
}
