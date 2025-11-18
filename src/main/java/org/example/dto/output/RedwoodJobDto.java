package org.example.dto.output;

import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Redwood job DTO for API calls
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedwoodJobDto {

    private String name;

    private String type;

    private String system;

    private RedwoodTriggerDto trigger;

    private Integer priority;

    private Integer maxRuntimeMinutes;

    @Builder.Default
    private List<String> notes = new ArrayList<>();

    @Builder.Default
    private java.util.Map<String, String> metadata = new java.util.HashMap<>();


    public void addNote(String note) {
        if (notes == null) {
            notes = new ArrayList<>();
        }
        notes.add(note);
    }


    public void addMetadata(String key, String value) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put(key, value);
    }
}
