
package com.meetme.dto;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MeetingRequestDTO {
    private Long ownerId;
    private List<Long> participantIds;
    private LocalDateTime startTime;
    private Duration duration;

}
