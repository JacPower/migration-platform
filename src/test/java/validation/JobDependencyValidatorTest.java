package validation;

import org.example.dto.input.ExportDataDto;
import org.example.dto.input.DependencyDto;
import org.example.dto.input.JobDto;
import org.example.dto.input.TriggerDto;
import org.example.dto.internal.ValidationResult;
import org.example.service.TriggerType;
import org.example.validator.JobDependencyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JobDependencyValidator Tests")
class JobDependencyValidatorTest {

    private JobDependencyValidator validator;



    @BeforeEach
    void setUp() {
        validator = new JobDependencyValidator();
    }



    @Test
    void validate_shouldPass_whenAllJobIdsAreUnique() {
        ExportDataDto export = createExport(
                createJob(1001, "Job_1"),
                createJob(1002, "Job_2"),
                createJob(1003, "Job_3")
        );

        ValidationResult result = validator.validate(export);

        assertFalse(result.getErrors().stream().anyMatch(e -> e.contains("Duplicate")));
    }



    @Test
    void validate_shouldPass_whenNoCircularDependenciesExist() {
        JobDto job1 = createJob(1001, "Job_1");
        JobDto job2 = createJobWithDeps(1002, "Job_2", 1001);
        JobDto job3 = createJobWithDeps(1003, "Job_3", 1002);

        ValidationResult result = validator.validate(createExport(job1, job2, job3));

        assertFalse(result.getErrors().stream().anyMatch(e -> e.contains("Circular")));
    }



    @Test
    void validate_shouldDetectCircularDependency_whenSimpleCycleExists() {
        JobDto job1 = createJobWithDeps(1001, "Job_1", 1002);
        JobDto job2 = createJobWithDeps(1002, "Job_2", 1001);

        ValidationResult result = validator.validate(createExport(job1, job2));

        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Circular dependency")));
    }



    @Test
    void validate_shouldDetectComplexCircularDependency_whenLongCycleExists() {
        JobDto job1 = createJobWithDeps(1001, "Job_1", 1004);
        JobDto job2 = createJobWithDeps(1002, "Job_2", 1001);
        JobDto job3 = createJobWithDeps(1003, "Job_3", 1002);
        JobDto job4 = createJobWithDeps(1004, "Job_4", 1003);

        ValidationResult result = validator.validate(createExport(job1, job2, job3, job4));

        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Circular dependency")));
    }



    @Test
    void validate_shouldDetectSelfReferencingDependency_whenJobDependsOnItself() {
        JobDto job = createJobWithDeps(1001, "Job_1", 1001);

        ValidationResult result = validator.validate(createExport(job));

        assertTrue(result.hasErrors());
    }



    @Test
    void validate_shouldPass_whenAllDependenciesExist() {
        JobDto job1 = createJob(1001, "Job_1");
        JobDto job2 = createJobWithDeps(1002, "Job_2", 1001);

        ValidationResult result = validator.validate(createExport(job1, job2));

        assertFalse(result.getErrors().stream().anyMatch(e -> e.contains("non-existent")));
    }



    @Test
    void validate_shouldDetectMissingDependency_whenDependencyDoesNotExist() {
        JobDto job = createJobWithDeps(1001, "Job_1", 9999);

        ValidationResult result = validator.validate(createExport(job));

        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("non-existent job 9999")));
    }



    @Test
    void validate_shouldDetectMultipleMissingDependencies_whenMultipleDepsMissing() {
        JobDto job1 = createJobWithDeps(1001, "Job_1", 9998);
        JobDto job2 = createJobWithDeps(1002, "Job_2", 9999);

        ValidationResult result = validator.validate(createExport(job1, job2));

        long missingErrors = result.getErrors().stream().filter(e -> e.contains("non-existent")).count();
        assertEquals(2, missingErrors);
    }



    @Test
    void validate_shouldPass_whenScheduleTriggerIsValid() {
        JobDto job = createJob(1001, "Job_1");

        ValidationResult result = validator.validate(createExport(job));

        assertFalse(result.getErrors().stream().anyMatch(e -> e.contains("cron")));
    }



    @Test
    void validate_shouldNotWarn_whenScheduledJobWithoutDependencies() {
        JobDto job = createJob(1001, "Scheduled_Job");

        ValidationResult result = validator.validate(createExport(job));

        assertFalse(result.getWarnings().stream().anyMatch(w -> w.contains("How will it be triggered")));
    }



    @Test
    void validate_shouldPass_whenCompleteExportIsValid() {
        JobDto job1 = createJob(1001, "Root_Job");
        JobDto job2 = createJobWithDeps(1002, "Child_Job", 1001);
        JobDto job3 = createJobWithDeps(1003, "Grandchild_Job", 1002);

        ValidationResult result = validator.validate(createExport(job1, job2, job3));

        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
    }



    @Test
    void validate_shouldPass_whenExportIsEmpty() {
        ExportDataDto export = ExportDataDto.builder().jobs(Collections.emptyList()).build();

        ValidationResult result = validator.validate(export);

        assertTrue(result.isValid());
    }



    private ExportDataDto createExport(JobDto... jobs) {
        return ExportDataDto.builder().jobs(Arrays.asList(jobs)).build();
    }



    private JobDto createJob(int id, String name) {
        TriggerDto trigger = TriggerDto.builder().type(TriggerType.SCHEDULE).cronExpression("0 2 * * *").build();

        return JobDto.builder()
                .jobId(id)
                .jobName(name)
                .jobType("BACKUP")
                .system("ORACLE")
                .trigger(trigger)
                .dependencies(Collections.emptyList())
                .build();
    }



    private JobDto createJobWithDeps(int id, String name, int... depIds) {
        List<DependencyDto> deps = Arrays.stream(depIds)
                .mapToObj(depId -> DependencyDto.builder().dependsOnJobId(depId).requiredStatus("SUCCESS").build())
                .toList();

        TriggerDto trigger = TriggerDto.builder().type(TriggerType.SCHEDULE).cronExpression("0 2 * * *").build();

        return JobDto.builder()
                .jobId(id)
                .jobName(name)
                .jobType("BACKUP")
                .system("ORACLE")
                .trigger(trigger)
                .dependencies(deps)
                .build();
    }
}

