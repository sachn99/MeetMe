
package com.meetme.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TimeSlotDTO {
    private LocalDateTime start;
    private LocalDateTime end;

    public TimeSlotDTO(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    // Getters and setters
}
