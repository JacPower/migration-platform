package org.example.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public final class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();



    private JsonUtils() {
    }



    public static <T> T parseJsonFile(String filePath, Class<T> clazz) throws IOException {
        if (!FileUtils.fileExists(filePath)) {
            throw new IOException("File not found: " + filePath);
        }

        String content = FileUtils.getFileContent(filePath);
        return objectMapper.readValue(content, clazz);
    }



    public static String toJsonString(Object dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to string: ", e);
            return "";
        }
    }
}
