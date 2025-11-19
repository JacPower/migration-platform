package org.example.validator;

import org.example.dto.input.CompetitorExportDto;
import org.example.dto.internal.ValidationResult;

public interface ExportValidator {
    ValidationResult validate(CompetitorExportDto export);

    void shutdown();
}
