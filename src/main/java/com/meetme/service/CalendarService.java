
package com.meetme.service;

import com.meetme.entities.User;
import com.meetme.exception.MeetingConflictException;
import com.meetme.exception.UserNotFoundException;
import com.meetme.repository.MeetingRepository;
import com.meetme.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.meetme.entities.Calendar;
import com.meetme.entities.Meeting;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class CalendarService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    private static final Logger logger = LoggerFactory.getLogger(CalendarService.class);

    public Meeting bookMeeting(Long ownerId, LocalDateTime startTime, Duration duration, List<Long> participantIds) {
        logger.info("Attempting to book a meeting for user ID: {}", ownerId);


        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("User "+ownerId+" not found."));

        LocalDateTime endTime = startTime.plus(duration);
        List<Meeting> conflicts = getConflicts(owner.getCalendar().getMeetings(), startTime, endTime);

        if (!conflicts.isEmpty()) {
            logger.warn("Meeting conflict detected for time range: {} - {}", startTime, endTime);
            throw new MeetingConflictException("Meeting conflict detected");
        }

        Meeting meeting = new Meeting();
        meeting.setStartTime(startTime);
        meeting.setEndTime(endTime);
        meeting.setCalendar(owner.getCalendar());

        List<User> participants = participantIds.stream()
                .map(id -> userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User "+ownerId+" not found.")))
                .collect(Collectors.toList());

        meeting.setParticipants(participants);
        owner.getCalendar().getMeetings().add(meeting);

        logger.info("Meeting successfully booked for user ID: {} from {} to {}", ownerId, startTime, endTime);
        return meetingRepository.save(meeting);
    }

    public List<User> checkConflicts(List<Long> participantIds, LocalDateTime startTime, Duration duration) {
        LocalDateTime endTime = startTime.plus(duration);
        List<User> conflicts = new ArrayList<>();

        for (Long id : participantIds) {
            User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
            if (!getConflicts(user.getCalendar().getMeetings(), startTime, endTime).isEmpty()) {
                conflicts.add(user);
            }
        }

        return conflicts;
    }
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

    public List<LocalDateTime[]> getFreeSlots(Calendar calendar1, Calendar calendar2, Duration duration) {
        LocalTime workStart = LocalTime.of(9, 0);
        LocalTime workEnd = LocalTime.of(17, 0);
        List<LocalDateTime[]> freeSlots = new ArrayList<>();
        LocalDateTime currentStart = LocalDateTime.now().with(workStart).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime workEndToday = LocalDateTime.now().with(workEnd).truncatedTo(ChronoUnit.SECONDS);

        List<Meeting> allMeetings = new ArrayList<>(calendar1.getMeetings());
        allMeetings.addAll(calendar2.getMeetings());
        allMeetings.sort((m1, m2) -> m1.getStartTime().compareTo(m2.getStartTime()));

        for (Meeting meeting : allMeetings) {
            LocalDateTime meetingStart = meeting.getStartTime().truncatedTo(ChronoUnit.MINUTES);
            LocalDateTime meetingEnd = meeting.getEndTime().truncatedTo(ChronoUnit.MINUTES);

            if (meetingStart.isAfter(workEndToday)) break;
            if (meetingEnd.isBefore(currentStart)) continue;

            if (meetingStart.isBefore(currentStart)) {
                meetingStart = currentStart;
            }

            if (Duration.between(currentStart, meetingStart).compareTo(duration) >= 0) {
                freeSlots.add(new LocalDateTime[]{currentStart, meetingStart});
            }

            if (meetingEnd.isAfter(currentStart)) {
                currentStart = meetingEnd;
            }
        }

        if (Duration.between(currentStart, workEndToday).compareTo(duration) >= 0) {
            freeSlots.add(new LocalDateTime[]{currentStart, workEndToday});
        }

        logger.info("Found {} free slots for duration {}: {}", freeSlots.size(), duration, freeSlots);
        return freeSlots;
    }


}

