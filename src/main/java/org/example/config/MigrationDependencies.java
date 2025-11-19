package org.example.config;

import org.example.parser.BatchFileParser;
import org.example.parser.ConcurrentJsonFileParser;
import org.example.parser.JsonFileParser;
import org.example.parser.DataParser;
import org.example.service.TriggerMigrationService;
import org.example.validator.ConcurrentJobValidator;
import org.example.validator.Validator;

public record MigrationDependencies(
        DataParser dataParser,
        BatchFileParser batchFileParser,
        Validator validator,
        TriggerMigrationService triggerService
) {
    public static MigrationDependencies createDefault() {
        return new MigrationDependencies(new JsonFileParser(), new ConcurrentJsonFileParser(), new ConcurrentJobValidator(), new TriggerMigrationService());
    }
}
