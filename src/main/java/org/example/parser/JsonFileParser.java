package org.example.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.input.ExportDataDto;
import org.example.utils.FileUtils;
import org.example.utils.JsonUtils;

import java.io.File;
import java.io.IOException;


@Slf4j
public class JsonFileParser implements DataParser {

    @Override
    public ExportDataDto parse(String filePath) throws IOException {
        log.info("Parsing JSON file: {}", filePath);

        ExportDataDto export = JsonUtils.parseJsonFile(filePath, ExportDataDto.class);
        log.info("Parsed {} jobs from {}", export.getJobs().size(), filePath);

        return export;
    }
}

