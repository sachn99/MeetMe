package com.meetme.controller;

import com.meetme.dto.MeetingRequestDTO;
import com.meetme.entities.Calendar;
import com.meetme.entities.User;
import com.meetme.exception.MeetingConflictException;
import com.meetme.exception.UserNotFoundException;
import com.meetme.repository.UserRepository;
import com.meetme.service.CalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CalendarControllerTest {

    @InjectMocks
    private CalendarController calendarController;

    @Mock
    private CalendarService calendarService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBookMeeting_Success() {

        MeetingRequestDTO meetingRequest = new MeetingRequestDTO();
        meetingRequest.setOwnerId(1L);
        meetingRequest.setStartTime(LocalDateTime.now().plusHours(1));
        meetingRequest.setDuration(Duration.ofMinutes(30));
        meetingRequest.setParticipantIds(List.of(2L,3L));

        ResponseEntity<String> response = calendarController.bookMeeting(meetingRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Meeting booked successfully.", response.getBody());
        verify(calendarService, times(1)).bookMeeting(anyLong(), any(LocalDateTime.class), any(Duration.class), anyList());
    }

    // Test for booking a meeting with conflict
    @Test
    void testBookMeeting_Conflict() {

        MeetingRequestDTO meetingRequest = new MeetingRequestDTO();
        meetingRequest.setOwnerId(1L);
        meetingRequest.setStartTime(LocalDateTime.now().plusHours(1));
        meetingRequest.setDuration(Duration.ofMinutes(30));
        meetingRequest.setParticipantIds(List.of(2L,3L));

        doThrow(new MeetingConflictException("Conflict detected")).when(calendarService).bookMeeting(anyLong(), any(LocalDateTime.class), any(Duration.class), anyList());

        ResponseEntity<String> response = calendarController.bookMeeting(meetingRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflict detected", response.getBody());
        verify(calendarService, times(1)).bookMeeting(anyLong(), any(LocalDateTime.class), any(Duration.class), anyList());
    }

    @Test
    void testBookMeeting_ServerError() {

        MeetingRequestDTO meetingRequest = new MeetingRequestDTO();
        meetingRequest.setOwnerId(1L);
        meetingRequest.setStartTime(LocalDateTime.now().plusHours(1));
        meetingRequest.setDuration(Duration.ofMinutes(30));
        meetingRequest.setParticipantIds(List.of(2L,3L));

        doThrow(new RuntimeException("Unexpected error")).when(calendarService).bookMeeting(anyLong(), any(LocalDateTime.class), any(Duration.class), anyList());

        ResponseEntity<String> response = calendarController.bookMeeting(meetingRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error booking meeting.", response.getBody());
        verify(calendarService, times(1)).bookMeeting(anyLong(), any(LocalDateTime.class), any(Duration.class), anyList());
    }

    // Test for checking conflicts successfully
    @Test
    void testCheckConflicts_Success() {

        MeetingRequestDTO meetingRequest = new MeetingRequestDTO();
        meetingRequest.setOwnerId(1L);
        meetingRequest.setStartTime(LocalDateTime.now().plusHours(1));
        meetingRequest.setDuration(Duration.ofMinutes(30));
        meetingRequest.setParticipantIds(List.of(2L,3L));

        when(calendarService.checkConflicts(anyList(), any(LocalDateTime.class), any(Duration.class)))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<User>> response = calendarController.checkConflicts(meetingRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(calendarService, times(1)).checkConflicts(anyList(), any(LocalDateTime.class), any(Duration.class));
    }

    // Test for server error during conflict check
    @Test
    void testCheckConflicts_ServerError() {
        MeetingRequestDTO meetingRequest = new MeetingRequestDTO();
        meetingRequest.setOwnerId(1L);
        meetingRequest.setStartTime(LocalDateTime.now().plusHours(1));
        meetingRequest.setDuration(Duration.ofMinutes(30));
        meetingRequest.setParticipantIds(List.of(2L,3L));

        doThrow(new RuntimeException("Unexpected error")).when(calendarService).checkConflicts(anyList(), any(LocalDateTime.class), any(Duration.class));

        ResponseEntity<List<User>> response = calendarController.checkConflicts(meetingRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(null, response.getBody());
        verify(calendarService, times(1)).checkConflicts(anyList(), any(LocalDateTime.class), any(Duration.class));
    }

    // Test for retrieving available slots successfully
    @Test
    void testGetAvailableSlots_Success() {
        Duration duration = Duration.ofMinutes(30);
        User user1 = new User();
        user1.setId(1L);
        user1.setCalendar(new Calendar());
        User user2 = new User();
        user2.setId(2L);
        user2.setCalendar(new Calendar());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(calendarService.getFreeSlots(any(), any(), eq(duration)))
                .thenReturn(List.<LocalDateTime[]>of(new LocalDateTime[]{LocalDateTime.now(), LocalDateTime.now().plusMinutes(30)}));

        ResponseEntity<List<LocalDateTime[]>> response = calendarController.getAvailableSlots(1L, 2L, duration.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(calendarService, times(1)).getFreeSlots(any(), any(), eq(duration));
    }

    // Test for user not found in availability check
    @Test
    void testGetAvailableSlots_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<List<LocalDateTime[]>> response = calendarController.getAvailableSlots(1L, 2L, "PT30M");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(null, response.getBody());
        verify(userRepository, times(1)).findById(1L);
    }

    // Test for unexpected server error in availability check
    @Test
    void testGetAvailableSlots_ServerError() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));
        doThrow(new RuntimeException("Unexpected error")).when(calendarService).getFreeSlots(any(), any(), any(Duration.class));

        ResponseEntity<List<LocalDateTime[]>> response = calendarController.getAvailableSlots(1L, 2L, "PT30M");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(null, response.getBody());
        verify(calendarService, times(1)).getFreeSlots(any(), any(), any(Duration.class));
    }
}
