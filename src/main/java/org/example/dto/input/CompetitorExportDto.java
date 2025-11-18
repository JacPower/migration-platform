package org.example.dto.input;

import lombok.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitorExportDto {

    @Valid
    @NotNull(message = "Jobs list cannot be null")
    @Builder.Default
    private List<JobDto> jobs = new ArrayList<>();
}