/*
package com.meetme.controller;

import com.meetme.dto.MeetingRequestDTO;
import com.meetme.dto.TimeSlotDTO;
import com.meetme.entities.Calendar;
import com.meetme.entities.User;
import com.meetme.service.CalendarService;
import com.meetme.service.MeetingService;
import com.meetme.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CalendarControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CalendarService calendarService;

    @Mock
    private MeetingService meetingService;

    @InjectMocks
    private CalendarController calendarController;

    private User user;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        Calendar calendar = new Calendar();
        user.setCalendar(calendar);
    }

    @Test
    public void testGetAvailableSlots_Success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(calendarService.getAvailableSlots(any(Calendar.class), any(Duration.class)))
                .thenReturn(List.of(new TimeSlotDTO(LocalDateTime.now(), LocalDateTime.now().plusMinutes(30))));

        mockMvc.perform(get("/api/calendar/availability")
                        .param("userId", "1L")
                        .param("duration", "PT30M"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(calendarService, times(1)).getAvailableSlots(any(Calendar.class), any(Duration.class));
    }

    @Test
    public void testGetAvailableSlots_UserNotFound() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/calendar/availability")
                        .param("userId", "1L")
                        .param("duration", "PT30M"))
                .andExpect(status().isNotFound());

        verify(calendarService, never()).getAvailableSlots(any(Calendar.class), any(Duration.class));
    }

    @Test
    public void testGetConflicts_Success() throws Exception {
        MeetingRequestDTO request = new MeetingRequestDTO();
        request.setParticipantIds(List.of(1L, 2L));
        request.setStartTime(LocalDateTime.now());
        request.setDuration(Duration.ofMinutes(30));

        when(meetingService.checkConflicts(anyList(), any(MeetingRequestDTO.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/calendar/conflicts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"participantIds\": [1, 2], \"startTime\": \"2024-11-10T10:00:00\", \"duration\": \"PT30M\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(meetingService, times(1)).checkConflicts(anyList(), any(MeetingRequestDTO.class));
    }

    @Test
    public void testGetConflicts_WithConflicts() throws Exception {
        MeetingRequestDTO request = new MeetingRequestDTO();
        request.setParticipantIds(List.of(1L, 2L));
        request.setStartTime(LocalDateTime.now());
        request.setDuration(Duration.ofMinutes(30));

        User conflictingUser = new User();
        conflictingUser.setId(2L);
        when(meetingService.checkConflicts(anyList(), any(MeetingRequestDTO.class)))
                .thenReturn(List.of(conflictingUser));

        mockMvc.perform(post("/api/calendar/conflicts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"participantIds\": [1, 2], \"startTime\": \"2024-11-10T10:00:00\", \"duration\": \"PT30M\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(meetingService, times(1)).checkConflicts(anyList(), any(MeetingRequestDTO.class));
    }
}
*/
