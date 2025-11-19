package org.example.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.input.CompetitorExportDto;

import java.io.File;
import java.io.IOException;


@Slf4j
public class CompetitorDataParser implements DataParser {

    private final ObjectMapper objectMapper;



    public CompetitorDataParser() {
        this.objectMapper = new ObjectMapper();
    }



    @Override
    public CompetitorExportDto parse(String filePath) throws IOException {
        log.info("Parsing JSON file: {}", filePath);

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        CompetitorExportDto export = objectMapper.readValue(file, CompetitorExportDto.class);
        log.info("Parsed {} jobs from {}", export.getJobs().size(), filePath);

        return export;
    }
}

