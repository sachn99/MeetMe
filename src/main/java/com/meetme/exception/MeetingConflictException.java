package com.meetme.exception;

public class MeetingConflictException extends RuntimeException {
    public MeetingConflictException(String message) {
        super(message);
    }
}
