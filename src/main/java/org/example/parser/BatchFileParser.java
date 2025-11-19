package org.example.parser;

import org.example.dto.input.JobDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface BatchFileParser {
    CompletableFuture<List<JobDto>> parseMultipleFiles(List<String> filePaths);

    void shutdown();
}
