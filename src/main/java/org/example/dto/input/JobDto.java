package org.example.dto.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobDto {

    @NotNull(message = "Job ID is required")
    @Positive(message = "Job ID must be positive")
    private Integer jobId;

    @NotBlank(message = "Job name is required")
    private String jobName;

    @NotBlank(message = "Job type is required")
    private String jobType;

    @NotBlank(message = "System is required")
    private String system;

    @Valid
    @NotNull(message = "Trigger configuration is required")
    private TriggerDto trigger;

    @Valid
    @JsonProperty("dependencies")
    @Builder.Default
    private List<DependencyDto> dependencies = new ArrayList<>();

    @Valid
    private ExecutionConstraintsDto executionConstraints;

    @NotBlank(message = "Note is required")
    private String notes;
}
