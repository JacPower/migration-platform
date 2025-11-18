package org.example.parser;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.input.CompetitorExportDto;
import org.example.dto.input.JobDto;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
public class ConcurrentFileParser {

    private final ExecutorService executor;
    private final CompetitorDataParser parser;



    public ConcurrentFileParser() {
        int threads = Math.min(2, Runtime.getRuntime().availableProcessors());
        this.executor = Executors.newFixedThreadPool(threads);
        this.parser = new CompetitorDataParser();
        log.info("Initialized concurrent parser with {} threads...", threads);
    }



    public CompletableFuture<List<JobDto>> parseMultipleFiles(List<String> filePaths) {
        log.info("Parsing {} files concurrently", filePaths.size());

        List<CompletableFuture<List<JobDto>>> futures = filePaths.stream()
                .map(path -> CompletableFuture.supplyAsync(
                        () -> parseFile(path),
                        executor
                ))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(List::stream)
                        .collect(Collectors.toList())
                );
    }



    private List<JobDto> parseFile(String filePath) {
        try {
            log.info("[{}] Parsing: {}", Thread.currentThread().getName(), filePath);

            CompetitorExportDto export = parser.parseJson(filePath);
            return export.getJobs();

        } catch (IOException e) {
            log.error("Failed to parse {}: {}", filePath, e.getMessage());
            return Collections.emptyList();
        }
    }



    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
