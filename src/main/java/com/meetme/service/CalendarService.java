
package com.meetme.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.meetme.dto.TimeSlotDTO;
import com.meetme.entities.Calendar;
import com.meetme.entities.Meeting;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class CalendarService {

    private static final Logger logger = LoggerFactory.getLogger(CalendarService.class);

    public List<Meeting> getConflicts(List<Meeting> existingMeetings, LocalDateTime start, LocalDateTime end) {
        logger.info("Checking for meeting conflicts between {} and {}", start, end);

        List<Meeting> conflicts = existingMeetings.stream()
                .filter(meeting -> meeting.getEndTime().isAfter(start) && meeting.getStartTime().isBefore(end))
                .collect(Collectors.toList());

        if (!conflicts.isEmpty()) {
            logger.warn("Found {} conflicting meetings during the specified time range", conflicts.size());
        } else {
            logger.info("No conflicts found during the specified time range");
        }

        return conflicts;
    }

    public List<TimeSlotDTO> getAvailableSlots(Calendar calendar, Duration duration) {
        logger.info("Calculating available time slots for duration: {}", duration);

        List<Meeting> sortedMeetings = calendar.getMeetings().stream()
                .sorted(Comparator.comparing(Meeting::getStartTime))
                .collect(Collectors.toList());

        List<TimeSlotDTO> availableSlots = new ArrayList<>();
        LocalDateTime currentTime = LocalDateTime.now().withHour(9).withMinute(0); // Assume working hours start at 9 AM
        LocalDateTime endOfDay = LocalDateTime.now().withHour(17).withMinute(0);   // Assume working hours end at 5 PM

        for (Meeting meeting : sortedMeetings) {
            if (Duration.between(currentTime, meeting.getStartTime()).compareTo(duration) >= 0) {
                availableSlots.add(new TimeSlotDTO(currentTime, meeting.getStartTime()));
                logger.debug("Available slot found from {} to {}", currentTime, meeting.getStartTime());
            }
            currentTime = meeting.getEndTime();
        }

        if (Duration.between(currentTime, endOfDay).compareTo(duration) >= 0) {
            availableSlots.add(new TimeSlotDTO(currentTime, endOfDay));
            logger.debug("Available slot found from {} to {}", currentTime, endOfDay);
        }

        logger.info("Total available slots found: {}", availableSlots.size());
        return availableSlots;
    }
}

