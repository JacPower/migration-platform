package org.example.dto.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.service.TriggerType;

import javax.validation.constraints.NotBlank;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriggerDto {

    @NotBlank(message = "Trigger type is required")
    private TriggerType type;

    private String cronExpression;

    private String timezone;

    private Integer upstreamJobId;

    private String watchPath;

    private String filePattern;

    private String eventSource;

    private String eventType;
}
