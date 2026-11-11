package com.example.studentapi.dto;

import com.example.studentapi.model.Employer.Kind;

// DTO = Data Transfer Object: a small class that only carries data between client and server.
// This one is the JSON body the client SENDS when creating or editing a profile.
// Why not accept the Employer entity directly? Because then a client could send
// "verificationStatus": "VERIFIED" and approve themselves. The DTO lists only the
// fields a client is allowed to set.
//
// "record" is a short way to write a class that just holds values. Java creates the
// constructor and the read methods (name(), kind(), ...) for us.
public record EmployerRequest(
        String name, // business name (required)
        Kind kind, // HOUSEHOLD, SME, COMPANY, AGENCY or NGO (required)
        String address,
        Double latitude, // map pin
        Double longitude,
        String contactPerson) {
}