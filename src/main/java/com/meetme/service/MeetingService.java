
package com.meetme.service;


import com.meetme.repository.MeetingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.meetme.repository.UserRepository;


@Service
public class MeetingService {
    private static final Logger logger = LoggerFactory.getLogger(MeetingService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private MeetingRepository meetingRepository;


/*
    public List<User> checkConflicts(List<Long> participantIds, LocalDateTime startTime, Duration duration) {
        LocalDateTime endTime = startTime.plus(duration);
        List<User> conflicts = new ArrayList<>();

        for (Long id : participantIds) {
            User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
            List<Meeting> conflictingMeetings = getConflicts(user.getCalendar().getMeetings(), startTime, endTime);
            if (!conflictingMeetings.isEmpty()) {
                conflicts.add(user);
            }
        }
        return conflicts;
    }
*/

/*    public List<User> checkConflicts(List<Long> participantIds, MeetingRequestDTO meetingRequest) {
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
    }*/
}
