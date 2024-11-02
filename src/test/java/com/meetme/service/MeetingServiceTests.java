package com.meetme.service;

import com.meetme.dto.MeetingRequestDTO;
import com.meetme.entities.Calendar;
import com.meetme.entities.Meeting;
import com.meetme.entities.User;
import com.meetme.exception.MeetingConflictException;
import com.meetme.exception.UserNotFoundException;
import com.meetme.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class MeetingServiceTests {

    @InjectMocks
    private MeetingService meetingService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CalendarService calendarService;

    private User owner;
    private MeetingRequestDTO meetingRequest;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        owner = new User();
        owner.setId(1L);
        Calendar calendar = new Calendar();
        owner.setCalendar(calendar);

        meetingRequest = new MeetingRequestDTO();
        meetingRequest.setStartTime(LocalDateTime.of(2024, 11, 10, 10, 0));
        meetingRequest.setDuration(Duration.ofHours(1));
        meetingRequest.setParticipantIds(Collections.singletonList(2L));
    }

    @Test
    public void testBookMeeting_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(calendarService.getConflicts(anyList(), any(), any())).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> meetingService.bookMeeting(1L, meetingRequest));
        verify(userRepository, times(1)).save(owner);
    }

    @Test
    public void testBookMeeting_WithConflict() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        Meeting conflictingMeeting = new Meeting();
        when(calendarService.getConflicts(anyList(), any(), any())).thenReturn(Collections.singletonList(conflictingMeeting));

        assertThrows(MeetingConflictException.class, () -> meetingService.bookMeeting(1L, meetingRequest));
    }

    @Test
    public void testBookMeeting_OwnerNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> meetingService.bookMeeting(1L, meetingRequest));
    }

    @Test
    public void testBookMeeting_ParticipantNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> meetingService.bookMeeting(1L, meetingRequest));
    }

    @Test
    public void testCheckConflicts_NoConflicts() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));
        when(calendarService.getConflicts(anyList(), any(), any())).thenReturn(Collections.emptyList());

        assertTrue(meetingService.checkConflicts(Collections.singletonList(2L), meetingRequest).isEmpty());
    }

    @Test
    public void testCheckConflicts_WithConflicts() {
        User participant = new User();
        participant.setId(2L);
        Calendar calendar = new Calendar();
        calendar.getMeetings().add(new Meeting());
        participant.setCalendar(calendar);

        when(userRepository.findById(2L)).thenReturn(Optional.of(participant));
        when(calendarService.getConflicts(anyList(), any(), any())).thenReturn(Collections.singletonList(new Meeting()));

        assertEquals(1, meetingService.checkConflicts(Collections.singletonList(2L), meetingRequest).size());
    }
}
