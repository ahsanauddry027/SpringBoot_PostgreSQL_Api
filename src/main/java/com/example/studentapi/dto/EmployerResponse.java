package com.example.studentapi.dto;

import java.time.Instant;
import java.util.UUID;

import com.example.studentapi.model.Employer;
import com.example.studentapi.model.Employer.Kind;
import com.example.studentapi.model.Employer.VerificationStatus;

// The JSON the server SENDS BACK. This is the "View" of MVC.
// It hides what the client should not see: instead of the document's file name
// we only say whether a document was uploaded.
public record EmployerResponse(
        UUID id,
        String name,
        Kind kind,
        String address,
        Double latitude,
        Double longitude,
        String contactPerson,
        boolean documentUploaded,
        VerificationStatus verificationStatus,
        String verificationNote,
        Instant verifiedAt) {

    // A helper that turns a database object (model) into a response (view).
    // "static" means we call it on the class: EmployerResponse.from(employer).
    public static EmployerResponse from(Employer employer) {
        return new EmployerResponse(
                employer.getId(),
                employer.getName(),
                employer.getKind(),
                employer.getAddress(),
                employer.getLatitude(),
                employer.getLongitude(),
                employer.getContactPerson(),
                employer.getDocumentKey() != null, // true once a file has been uploaded
                employer.getVerificationStatus(),
                employer.getVerificationNote(),
                employer.getVerifiedAt());
    }
}