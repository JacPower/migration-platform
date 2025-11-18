package validation;

import org.example.dto.input.CompetitorExportDto;
import org.example.dto.input.DependencyDto;
import org.example.dto.input.JobDto;
import org.example.dto.input.TriggerDto;
import org.example.dto.internal.ValidationResult;
import org.example.service.TriggerType;
import org.example.validator.ConcurrentJobValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ConcurrentJobValidator Tests")
class ConcurrentJobValidatorTest {

    private ConcurrentJobValidator validator;



    @BeforeEach
    void setUp() {
        validator = new ConcurrentJobValidator();
    }



    @AfterEach
    void tearDown() {
        validator.shutdown();
    }



    @Test
    void validate_shouldUseSequentialValidation_whenBatchIsSmall() {
        List<JobDto> jobs = createJobs(5);
        CompetitorExportDto export = CompetitorExportDto.builder().jobs(jobs).build();

        ValidationResult result = validator.validate(export);

        assertTrue(result.isValid());
    }



    @Test
    void validate_shouldUseConcurrentValidation_whenBatchIsLarge() {
        List<JobDto> jobs = createJobs(100);
        CompetitorExportDto export = CompetitorExportDto.builder().jobs(jobs).build();

        long startTime = System.currentTimeMillis();
        ValidationResult result = validator.validate(export);
        long duration = System.currentTimeMillis() - startTime;

        assertTrue(result.isValid());
        System.out.println("Validated 100 jobs in " + duration + "ms");
    }



    @Test
    void validate_shouldDetectErrors_whenInvalidJobsExist() {
        List<JobDto> jobs = createJobs(50);
        jobs.add(createJobWithDeps(9999, "Invalid_Job", 88888));

        CompetitorExportDto export = CompetitorExportDto.builder().jobs(jobs).build();

        ValidationResult result = validator.validate(export);

        assertTrue(result.hasErrors());
    }



    @Test
    void validate_shouldBeThreadSafe_whenMultipleThreadsRunConcurrently() throws InterruptedException {
        List<JobDto> jobs = createJobs(100);
        CompetitorExportDto export = CompetitorExportDto.builder().jobs(jobs).build();

        List<ValidationResult> results = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Thread t = new Thread(() -> {
                ValidationResult result = validator.validate(export);
                synchronized (results) {
                    results.add(result);
                }
            });
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        assertEquals(5, results.size());
        assertTrue(results.stream().allMatch(ValidationResult::isValid));
    }




    private List<JobDto> createJobs(int count) {
        List<JobDto> jobs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            jobs.add(JobDto.builder()
                    .jobId(1000 + i)
                    .jobName("Job_" + i)
                    .jobType("BACKUP")
                    .system("ORACLE")
                    .trigger(TriggerDto.builder()
                            .type(TriggerType.SCHEDULE)
                            .cronExpression("0 2 * * *")
                            .build())
                    .dependencies(new ArrayList<>())
                    .build());
        }
        return jobs;
    }



    private JobDto createJobWithDeps(int id, String name, int... depIds) {
        List<DependencyDto> deps = new ArrayList<>();
        for (int depId : depIds) {
            deps.add(DependencyDto.builder()
                    .dependsOnJobId(depId)
                    .requiredStatus("SUCCESS")
                    .build());
        }

        return JobDto.builder()
                .jobId(id)
                .jobName(name)
                .jobType("BACKUP")
                .system("ORACLE")
                .trigger(TriggerDto.builder()
                        .type(TriggerType.SCHEDULE)
                        .cronExpression("0 2 * * *")
                        .build())
                .dependencies(deps)
                .build();
    }
}
