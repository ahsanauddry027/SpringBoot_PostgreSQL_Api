package com.example.studentapi.dto;

// The JSON body an admin sends to approve or reject an employer, for example
// {"decision": "APPROVE"}  or  {"decision": "REJECT", "reason": "Licence not readable"}.
public record VerificationDecision(String decision, String reason) {
}