package com.example.studentapi.dto;

// The JSON body an employer sends to change a post's status, for example
// {"action": "PAUSE"}. The allowed actions are PAUSE, RESUME, CLOSE and REPOST.
public record JobStatusChange(String action) {
}
