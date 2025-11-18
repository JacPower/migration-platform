package org.example.dto.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedwoodTriggerDto {

    private String type;

    private String schedule;

    private String timezone;

    private Boolean apiEnabled;

    private String preScript;
}
