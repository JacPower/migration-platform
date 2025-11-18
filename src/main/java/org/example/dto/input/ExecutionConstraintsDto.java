package org.example.dto.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionConstraintsDto {

    @Min(value = 1, message = "Priority must be at least 1")
    @Max(value = 10, message = "Priority must be at most 10")
    private Integer priority;

    @Positive(message = "Max runtime must be positive")
    private Integer maxRuntimeMinutes;

    @Builder.Default
    private Boolean excludeHolidays = false;
}
