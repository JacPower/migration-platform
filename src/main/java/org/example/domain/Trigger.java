package org.example.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


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
}
