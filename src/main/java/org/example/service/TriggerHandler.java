package org.example.service;


import org.example.dto.internal.Trigger;
import org.example.dto.internal.ValidationResult;
import org.example.dto.output.RedwoodJobDto;
import org.example.exception.MigrationException;

public interface TriggerHandler {

    TriggerType getSupportedType();

    ValidationResult validate(Trigger trigger);

    RedwoodJobDto migrate(Trigger trigger) throws MigrationException;

    String getDescription();
}
