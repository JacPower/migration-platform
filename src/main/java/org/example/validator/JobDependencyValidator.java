package org.example.validator;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.input.DependencyDto;
import org.example.dto.input.ExportDataDto;
import org.example.dto.input.JobDto;
import org.example.dto.internal.ValidationResult;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class JobDependencyValidator {

    public ValidationResult validate(ExportDataDto export) {
        List<JobDto> jobs = export.getJobs();
        ValidationResult result = new ValidationResult();

        Set<Integer> jobIds = collectJobIds(jobs, result);
        Map<Integer, List<Integer>> graph = buildDependencyGraph(jobs);

        validateReferences(graph, jobIds, result);
        validateNoCycles(graph, result);

        log.info("Validation complete: {} errors", result.getErrors().size());
        return result;
    }



    private Set<Integer> collectJobIds(List<JobDto> jobs, ValidationResult result) {
        Set<Integer> ids = new HashSet<>();

        for (JobDto job : jobs) {
            if (!ids.add(job.getJobId())) {
                result.addError("Duplicate job ID: " + job.getJobId());
            }
        }

        return ids;
    }



    private Map<Integer, List<Integer>> buildDependencyGraph(List<JobDto> jobs) {
        return jobs.stream()
                .collect(Collectors.toMap(
                        JobDto::getJobId,
                        job -> job.getDependencies().stream()
                                .map(DependencyDto::getDependsOnJobId)
                                .toList()
                ));
    }



    private void validateReferences(Map<Integer, List<Integer>> graph, Set<Integer> jobIds, ValidationResult result) {
        graph.forEach((jobId, dependencies) ->
                dependencies.stream()
                        .filter(depId -> !jobIds.contains(depId))
                        .forEach(depId -> result.addError("Job " + jobId + " depends on non-existent job " + depId))
        );
    }



    private void validateNoCycles(Map<Integer, List<Integer>> graph, ValidationResult result) {
        Set<Integer> visited = new HashSet<>();
        Set<Integer> inStack = new HashSet<>();

        for (Integer jobId : graph.keySet()) {
            if (hasCycle(jobId, graph, visited, inStack)) {
                result.addError("Circular dependency detected");
                return;
            }
        }
    }



    private boolean hasCycle(Integer jobId, Map<Integer, List<Integer>> graph, Set<Integer> visited, Set<Integer> inStack) {
        if (inStack.contains(jobId)) return true;
        if (visited.contains(jobId)) return false;

        visited.add(jobId);
        inStack.add(jobId);

        for (Integer dep : graph.getOrDefault(jobId, Collections.emptyList())) {
            if (hasCycle(dep, graph, visited, inStack)) {
                return true;
            }
        }

        inStack.remove(jobId);
        return false;
    }
}