package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.parser.CompetitorDataParser;
import org.example.parser.ConcurrentFileParser;
import org.example.validator.ConcurrentJobValidator;

@Slf4j
public class MigrationApplication {
    private final CompetitorDataParser parser;
    private final ConcurrentFileParser fileParser;
    private final ConcurrentJobValidator validator;
    //TODO: private final TriggerMigrationService triggerService;
    //TODO: private final ConcurrentRedwoodApiClient apiClient;

    public MigrationApplication(String redwoodApiUrl) {
        this.parser = new CompetitorDataParser();
        this.fileParser = new ConcurrentFileParser();
        this.validator = new ConcurrentJobValidator();
        //TODO: this.triggerService = new TriggerMigrationService();
        //TODO: this.apiClient = new ConcurrentRedwoodApiClient(redwoodApiUrl, 10);

        log.info("Migration application initialized...");
    }
}
