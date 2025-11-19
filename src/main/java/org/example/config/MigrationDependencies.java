package org.example.config;

import org.example.parser.BatchFileParser;
import org.example.parser.CompetitorDataParser;
import org.example.parser.ConcurrentFileParser;
import org.example.parser.DataParser;
import org.example.service.TriggerMigrationService;
import org.example.validator.ConcurrentJobValidator;
import org.example.validator.ExportValidator;

public record MigrationDependencies(
        DataParser dataParser,
        BatchFileParser batchFileParser,
        ExportValidator exportValidator,
        TriggerMigrationService triggerService
) {
    public static MigrationDependencies createDefault() {
        return new MigrationDependencies(new CompetitorDataParser(), new ConcurrentFileParser(), new ConcurrentJobValidator(), new TriggerMigrationService());
    }
}
