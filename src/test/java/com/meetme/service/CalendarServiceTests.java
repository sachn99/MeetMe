package com.meetme.service;

import com.meetme.dto.TimeSlotDTO;
import com.meetme.entities.Calendar;
import com.meetme.entities.Meeting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CalendarServiceTests {

    @InjectMocks
    private CalendarService calendarService;

    private List<Meeting> meetings;

    @BeforeEach
    public void setup() {
        meetings = new ArrayList<>();
    }

    @Test
    public void testGetConflicts_NoConflicts() {
        meetings.add(createMeeting("2024-11-10T09:00", "2024-11-10T10:00"));
        meetings.add(createMeeting("2024-11-10T11:00", "2024-11-10T12:00"));

        List<Meeting> conflicts = calendarService.getConflicts(meetings, LocalDateTime.of(2024, 11, 10, 12, 30),
                LocalDateTime.of(2024, 11, 10, 13, 30));

        assertTrue(conflicts.isEmpty());
    }

    @Test
    public void testGetConflicts_WithConflicts() {
        meetings.add(createMeeting("2024-11-10T09:00", "2024-11-10T10:00"));
        meetings.add(createMeeting("2024-11-10T10:30", "2024-11-10T11:30"));

        List<Meeting> conflicts = calendarService.getConflicts(meetings, LocalDateTime.of(2024, 11, 10, 10, 0),
                LocalDateTime.of(2024, 11, 10, 11, 0));

        assertEquals(1, conflicts.size());
    }

    @Test
    public void testGetAvailableSlots_NoMeetings() {
        Calendar calendar = new Calendar();
        calendar.setMeetings(meetings);

        List<TimeSlotDTO> availableSlots = calendarService.getAvailableSlots(calendar, Duration.ofMinutes(30));

        assertFalse(availableSlots.isEmpty());
    }

    @Test
    public void testGetAvailableSlots_WithMeetings() {
        Calendar calendar = new Calendar();
        meetings.add(createMeeting("2024-11-10T09:00", "2024-11-10T10:00"));
        meetings.add(createMeeting("2024-11-10T11:00", "2024-11-10T12:00"));
        calendar.setMeetings(meetings);

        List<TimeSlotDTO> availableSlots = calendarService.getAvailableSlots(calendar, Duration.ofMinutes(30));

        assertTrue(availableSlots.size() > 1);
    }

    @Test
    public void testGetAvailableSlots_NoAvailableSlots() {
        Calendar calendar = new Calendar();
        meetings.add(createMeeting("2024-11-10T09:00", "2024-11-10T17:00"));
        calendar.setMeetings(meetings);

        List<TimeSlotDTO> availableSlots = calendarService.getAvailableSlots(calendar, Duration.ofMinutes(30));

        assertTrue(availableSlots.isEmpty());
    }

    private Meeting createMeeting(String start, String end) {
        Meeting meeting = new Meeting();
        meeting.setStartTime(LocalDateTime.parse(start));
        meeting.setEndTime(LocalDateTime.parse(end));
        return meeting;
    }
}
