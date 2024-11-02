
package com.meetme.service;

import com.meetme.exception.MeetingConflictException;
import com.meetme.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.meetme.dto.MeetingRequestDTO;
import com.meetme.entities.Meeting;
import com.meetme.entities.User;
import com.meetme.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MeetingService {
    private static final Logger logger = LoggerFactory.getLogger(MeetingService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CalendarService calendarService;

    public void bookMeeting(Long ownerId, MeetingRequestDTO meetingRequest) {
        logger.info("Starting booking process for meeting with owner ID: {} and request: {}", ownerId, meetingRequest);
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", ownerId);
                    return new UserNotFoundException("User not found with ID: " + ownerId);
                });

        LocalDateTime startTime = meetingRequest.getStartTime();
        LocalDateTime endTime = startTime.plus(meetingRequest.getDuration());

        List<Meeting> conflicts = calendarService.getConflicts(owner.getCalendar().getMeetings(), startTime, endTime);
        if (!conflicts.isEmpty()) {
            logger.warn("Meeting conflict detected for time range {} - {}", startTime, endTime);
            throw new MeetingConflictException("Meeting conflict found with existing bookings.");
        }

        Meeting meeting = new Meeting();
        meeting.setStartTime(startTime);
        meeting.setEndTime(endTime);

        List<User> participants = meetingRequest.getParticipantIds().stream()
                .map(id -> userRepository.findById(id)
                        .orElseThrow(() -> {
                            logger.warn("User not found with ID: {}", id);
                            return new UserNotFoundException("User not found with ID: " + id);
                        }))
                .collect(Collectors.toList());

        meeting.setParticipants(participants);
        owner.getCalendar().getMeetings().add(meeting);
        userRepository.save(owner);
        logger.info("Meeting successfully booked from {} to {} for owner ID: {}", startTime, endTime, ownerId);
    }

    public List<User> checkConflicts(List<Long> participantIds, MeetingRequestDTO meetingRequest) {
        LocalDateTime startTime = meetingRequest.getStartTime();
        LocalDateTime endTime = startTime.plus(meetingRequest.getDuration());

        logger.info("Checking conflicts for meeting request from {} to {} for participant IDs: {}", startTime, endTime, participantIds);

        List<User> conflictingUsers = participantIds.stream()
                .map(id -> userRepository.findById(id)
                        .orElseThrow(() -> {
                            logger.warn("User not found with ID: {}", id);
                            return new UserNotFoundException("User not found with ID: " + id);
                        }))
                .filter(user -> !calendarService.getConflicts(user.getCalendar().getMeetings(), startTime, endTime).isEmpty())
                .collect(Collectors.toList());

        logger.info("Total conflicting users found: {}", conflictingUsers.size());
        return conflictingUsers;
    }
}
