
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
        meetingService.bookMeeting(meetingRequest.getOwnerId(), meetingRequest);
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

    @GetMapping("/availability")
    public ResponseEntity<List<TimeSlotDTO>> getAvailableSlots(@RequestParam Long userId, @RequestParam Duration duration) {
        logger.info("Received request to check available slots for user ID: {} with duration: {}", userId, duration);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
            List<TimeSlotDTO> availableSlots = calendarService.getAvailableSlots(user.getCalendar(), duration);
            logger.info("Available slots found for user ID {}: {}", userId, availableSlots.size());
            return ResponseEntity.ok(availableSlots);
        } catch (UserNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error retrieving available slots for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/conflicts")
    public ResponseEntity<List<User>> getConflicts(@RequestBody MeetingRequestDTO meetingRequest) {
        logger.info("Received request to check conflicts for meeting with participant IDs: {}", meetingRequest.getParticipantIds());
        try {
            List<User> conflictingUsers = meetingService.checkConflicts(meetingRequest.getParticipantIds(), meetingRequest);
            logger.info("Conflicts found: {} users have conflicting schedules", conflictingUsers.size());
            return ResponseEntity.ok(conflictingUsers);
        } catch (Exception e) {
            logger.error("Error checking conflicts for meeting request with participant IDs: {}", meetingRequest.getParticipantIds(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
