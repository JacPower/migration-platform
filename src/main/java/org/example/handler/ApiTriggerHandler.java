package org.example.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.internal.Trigger;
import org.example.dto.internal.ValidationResult;
import org.example.dto.output.RedwoodJobDto;
import org.example.dto.output.RedwoodTriggerDto;
import org.example.service.TriggerHandler;
import org.example.service.TriggerType;
import org.example.utils.Constants;
import org.example.utils.FileUtils;

import java.util.Date;


@Slf4j
public class ApiTriggerHandler implements TriggerHandler {

    @Override
    public TriggerType getSupportedType() {
        return TriggerType.API;
    }



    @Override
    public boolean canHandle(Trigger trigger) {
        return trigger.getType() == TriggerType.API;
    }



    @Override
    public ValidationResult validate(Trigger trigger) {
        return new ValidationResult();
    }



    @Override
    public RedwoodJobDto migrate(Trigger trigger) {
        log.info("Migrating API trigger for job: {}", trigger.getJobName());

        RedwoodTriggerDto redwoodTrigger = RedwoodTriggerDto.builder()
                .type("API")
                .apiEnabled(true)
                .build();

        RedwoodJobDto redwoodJobDto = RedwoodJobDto.builder()
                .name(trigger.getJobName())
                .type("API")
                .trigger(redwoodTrigger)
                .build();

        String outputFileName = redwoodJobDto.getName() + "_" + new Date().getTime() + ".json";
        String outputPath = Constants.DEFAULT_OUTPUT_FOLDER;
        FileUtils.writeToJsonFile(redwoodJobDto, outputFileName, outputPath);

        return redwoodJobDto;
    }



    @Override
    public String getDescription() {
        return "Direct migration - RMJ supports API triggers";
    }
}
