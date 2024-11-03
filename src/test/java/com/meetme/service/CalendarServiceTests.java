package com.meetme.service;

import com.meetme.entities.Calendar;
import com.meetme.entities.Meeting;
import com.meetme.entities.User;
import com.meetme.repository.MeetingRepository;
import com.meetme.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CalendarServiceTest {

    @InjectMocks
    private CalendarService calendarService;


    @InjectMocks
    private MeetingService meetingService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MeetingRepository meetingRepository;

    private User owner;
    private Calendar calendar;

    private User user1;
    private User user2;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize User and Calendar
        calendar = new Calendar();
        owner = new User();
        owner.setId(1L);
        owner.setCalendar(calendar);

        Calendar calendar1 = new Calendar();
        Calendar calendar2 = new Calendar();

        user1 = new User();
        user1.setId(1L);
        user1.setCalendar(calendar1);

        user2 = new User();
        user2.setId(2L);
        user2.setCalendar(calendar2);
    }

    @Test
    void testBookMeeting_SuccessfulBooking() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        Duration duration = Duration.ofMinutes(30);

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Meeting meeting = calendarService.bookMeeting(owner.getId(), startTime, duration, Collections.emptyList());

        assertNotNull(meeting);
        assertEquals(startTime, meeting.getStartTime());
        assertEquals(startTime.plus(duration), meeting.getEndTime());
        verify(meetingRepository, times(1)).save(meeting);
    }

    @Test
    void testBookMeeting_ConflictExists() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        Duration duration = Duration.ofMinutes(30);

        Meeting existingMeeting = new Meeting();
        existingMeeting.setStartTime(startTime.minusMinutes(15));
        existingMeeting.setEndTime(startTime.plusMinutes(15));
        calendar.setMeetings(Collections.singletonList(existingMeeting));

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                calendarService.bookMeeting(owner.getId(), startTime, duration, Collections.emptyList())
        );

        assertEquals("Meeting conflict detected", exception.getMessage());
        verify(meetingRepository, never()).save(any());
    }


    @Test
    void testCheckConflicts_NoConflicts() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        Duration duration = Duration.ofMinutes(30);
        User participant = new User();
        participant.setId(2L);
        participant.setCalendar(new Calendar());

        when(userRepository.findById(participant.getId())).thenReturn(Optional.of(participant));

        List<User> conflicts = calendarService.checkConflicts(Collections.singletonList(participant.getId()), startTime, duration);

        assertTrue(conflicts.isEmpty());
    }


    @Test
    void testCheckConflicts_WithConflicts() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        Duration duration = Duration.ofMinutes(30);
        User participant = new User();
        participant.setId(2L);
        Calendar participantCalendar = new Calendar();
        participant.setCalendar(participantCalendar);

        Meeting conflictingMeeting = new Meeting();
        conflictingMeeting.setStartTime(startTime.minusMinutes(15));
        conflictingMeeting.setEndTime(startTime.plusMinutes(15));
        participantCalendar.setMeetings(Collections.singletonList(conflictingMeeting));

        when(userRepository.findById(participant.getId())).thenReturn(Optional.of(participant));

        List<User> conflicts = calendarService.checkConflicts(Collections.singletonList(participant.getId()), startTime, duration);

        assertEquals(1, conflicts.size());
        assertEquals(participant.getId(), conflicts.get(0).getId());
    }

    @Test
    void testGetFreeSlots_NoMeetingsFullDayAvailable() {
        Duration duration = Duration.ofMinutes(30);

        List<LocalDateTime[]> freeSlots = calendarService.getFreeSlots(user1.getCalendar(), user2.getCalendar(), duration);

        // Expecting slots throughout the workday, i.e: 9:00 AM to 5:00 PM
        assertFalse(freeSlots.isEmpty());
        assertEquals(LocalTime.of(9, 0), freeSlots.get(0)[0].toLocalTime());
        assertEquals(LocalTime.of(17, 0), freeSlots.get(freeSlots.size() - 1)[1].toLocalTime());
    }

    @Test
    void testGetFreeSlots_WithGapsForMeeting() {
        Duration duration = Duration.ofMinutes(30);
        LocalDateTime now = LocalDateTime.now().withHour(9).withMinute(0).truncatedTo(ChronoUnit.SECONDS);

        Meeting user1Meeting = new Meeting();
        user1Meeting.setStartTime(now.plusHours(1));
        user1Meeting.setEndTime(now.plusHours(1).plusMinutes(30));
        user1.getCalendar().getMeetings().add(user1Meeting);

        Meeting user2Meeting = new Meeting();
        user2Meeting.setStartTime(now.plusHours(2));
        user2Meeting.setEndTime(now.plusHours(2).plusMinutes(30));
        user2.getCalendar().getMeetings().add(user2Meeting);

        List<LocalDateTime[]> freeSlots = calendarService.getFreeSlots(user1.getCalendar(), user2.getCalendar(), duration);

        assertFalse(freeSlots.isEmpty());
        assertEquals(LocalTime.of(9, 0), freeSlots.get(0)[0].toLocalTime().truncatedTo(ChronoUnit.MINUTES));
        assertEquals(LocalTime.of(10, 0), freeSlots.get(0)[1].toLocalTime().truncatedTo(ChronoUnit.MINUTES));

        assertEquals(LocalTime.of(10, 30), freeSlots.get(1)[0].toLocalTime().truncatedTo(ChronoUnit.MINUTES));
        assertEquals(LocalTime.of(11, 0), freeSlots.get(1)[1].toLocalTime().truncatedTo(ChronoUnit.MINUTES));

        assertEquals(LocalTime.of(11, 30), freeSlots.get(2)[0].toLocalTime().truncatedTo(ChronoUnit.MINUTES));
        assertEquals(LocalTime.of(17, 0), freeSlots.get(2)[1].toLocalTime().truncatedTo(ChronoUnit.MINUTES));
    }

    @Test
    void testGetFreeSlots_NoCommonFreeSlots() {
        Duration duration = Duration.ofHours(2);
        LocalDateTime now = LocalDateTime.now().withHour(9).withMinute(0);

        // User 1 full day booked
        Meeting user1Meeting = new Meeting();
        user1Meeting.setStartTime(now.plusHours(1));
        user1Meeting.setEndTime(now.plusHours(8));
        user1.getCalendar().getMeetings().add(user1Meeting);

        // User 2 full day booked
        Meeting user2Meeting = new Meeting();
        user2Meeting.setStartTime(now.plusHours(1));
        user2Meeting.setEndTime(now.plusHours(8));
        user2.getCalendar().getMeetings().add(user2Meeting);

        List<LocalDateTime[]> freeSlots = calendarService.getFreeSlots(user1.getCalendar(), user2.getCalendar(), duration);

        assertTrue(freeSlots.isEmpty());  // Expect no free slots for a 2-hour meeting
    }
}
