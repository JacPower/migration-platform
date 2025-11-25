package org.example.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.service.TriggerType;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trigger {

    private TriggerType type;
    private String jobName;

    private String cronExpression;
    private String timezone;

    private String watchPath;
    private String filePattern;

    private String eventSource;
    private String eventType;

    private Integer upstreamJobId;

    private String outputFolderPath;
}
