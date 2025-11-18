package org.example.dto.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompetitorExportDto {

    @Valid
    @NotNull(message = "Jobs list cannot be null")
    @Builder.Default
    private List<JobDto> jobs = new ArrayList<>();
}