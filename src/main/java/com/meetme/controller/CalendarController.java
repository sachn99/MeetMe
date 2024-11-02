
package com.meetme.controller;

import com.meetme.entities.User;
import com.meetme.exception.MeetingConflictException;
import com.meetme.exception.UserNotFoundException;
import com.meetme.repository.UserRepository;
import com.meetme.service.CalendarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.meetme.dto.MeetingRequestDTO;
import com.meetme.dto.TimeSlotDTO;
import com.meetme.service.MeetingService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private static final Logger logger = LoggerFactory.getLogger(CalendarController.class);

    @Autowired
    private MeetingService meetingService;
    @Autowired
    private CalendarService calendarService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/book")
    public ResponseEntity<String> bookMeeting(@RequestBody MeetingRequestDTO meetingRequest) {
        logger.info("Received request to book meeting: {}", meetingRequest);
    try {
        calendarService.bookMeeting(meetingRequest.getOwnerId(), meetingRequest.getStartTime(),meetingRequest.getDuration(),meetingRequest.getParticipantIds());
        logger.info("Meeting booked successfully.");
        return ResponseEntity.ok("Meeting booked successfully.");
    }
    catch (MeetingConflictException e) {
        logger.warn("Meeting conflict: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
    catch (Exception e){
        logger.error("Error booking meeting", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error booking meeting.");

     }
    }

    @PostMapping("/conflicts")
    public ResponseEntity<List<User>> checkConflicts(@RequestBody MeetingRequestDTO meetingRequest) {
        logger.info("Received request to check conflicts for meeting with participant IDs: {}", meetingRequest.getParticipantIds());
        try {
            List<User> conflictingUsers= calendarService.checkConflicts(meetingRequest.getParticipantIds(), meetingRequest.getStartTime(), meetingRequest.getDuration());
            logger.info("Conflicts found: {} users have conflicting schedules", conflictingUsers.size());

            return ResponseEntity.ok(conflictingUsers);
        } catch (Exception e) {
            logger.error("Error checking conflicts for meeting request with participant IDs: {}", meetingRequest.getParticipantIds(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


   @GetMapping("/availability")
    public ResponseEntity<List<LocalDateTime[]>> getAvailableSlots(@RequestParam Long userId1, @RequestParam Long userId2, @RequestParam String duration) {
        logger.info("Received request to check available slots for user IDs: {} and {} with duration: {}", userId1,userId2,duration);
        try {
            User user1 = userRepository.findById(userId1)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId1));

            User user2 = userRepository.findById(userId2)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId2));

            Duration dur = Duration.parse(duration);

            List<LocalDateTime[]> availableSlots = calendarService.getFreeSlots(user1.getCalendar(),user2.getCalendar(), dur);
            logger.info("Available slots found for user ID {} and {}: {}", userId1,user2, availableSlots.size());
            return ResponseEntity.ok(availableSlots);
        } catch (UserNotFoundException e) {
            logger.warn("User not found with ID: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error retrieving available slots for user IDs: {} and {}", userId1,userId2, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
