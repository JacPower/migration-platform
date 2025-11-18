package org.example.dto.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DependencyDto {

    @NotNull(message = "Dependent job ID is required")
    @Positive(message = "Dependent job ID must be positive")
    private Integer dependsOnJobId;

    @NotBlank(message = "Required status is required")
    private String requiredStatus;
}
