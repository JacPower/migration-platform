package org.example.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.input.ExportDataDto;

import java.io.File;
import java.io.IOException;


@Slf4j
public class JsonFileParser implements DataParser {

    private final ObjectMapper objectMapper;



    public JsonFileParser() {
        this.objectMapper = new ObjectMapper();
    }



    @Override
    public ExportDataDto parse(String filePath) throws IOException {
        log.info("Parsing JSON file: {}", filePath);

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        ExportDataDto export = objectMapper.readValue(file, ExportDataDto.class);
        log.info("Parsed {} jobs from {}", export.getJobs().size(), filePath);

        return export;
    }
}

