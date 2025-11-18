package org.example.dto.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriggerDto {

    @NotBlank(message = "Trigger type is required")
    private String type;

    private String cronExpression;

    private String timezone;

    private Integer upstreamJobId;

    private String watchPath;

    private String filePattern;

    private String eventSource;

    private String eventType;
}
