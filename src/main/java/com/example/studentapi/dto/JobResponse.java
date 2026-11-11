package com.example.studentapi.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.example.studentapi.model.Employer.VerificationStatus;
import com.example.studentapi.model.Job;
import com.example.studentapi.model.Job.JobType;
import com.example.studentapi.model.Job.Shift;
import com.example.studentapi.model.Job.Status;
import com.example.studentapi.model.Job.WagePeriod;

// The JSON the server SENDS BACK for a job. This is the "View" of MVC.
// Instead of the whole Employer object it carries just the owner's id and name,
// plus verifiedEmployer, which the screen shows as the "Verified" badge.
public record JobResponse(
        UUID id,
        Status status,                  // shown first: the most important fact about a post
        UUID employerId,
        String employerName,
        boolean verifiedEmployer,
        String titleBn,
        String titleEn,
        String descriptionBn,
        String descriptionEn,
        String category,
        JobType jobType,
        Shift shift,
        String hours,
        Integer openings,
        Integer wageMin,
        Integer wageMax,
        WagePeriod wagePeriod,
        Double latitude,
        Double longitude,
        boolean remote,
        Integer minExperienceYears,
        List<String> requiredSkills,
        List<String> requiredDocuments,
        Instant publishedAt,
        Instant expiresAt,
        Instant createdAt) {

    // Turns a database object (model) into a response (view).
    public static JobResponse from(Job job) {
        return new JobResponse(
                job.getId(),
                job.getStatus(),
                job.getEmployer().getId(),
                job.getEmployer().getName(),
                // true only when an admin has approved the employer
                job.getEmployer().getVerificationStatus() == VerificationStatus.VERIFIED,
                job.getTitleBn(),
                job.getTitleEn(),
                job.getDescriptionBn(),
                job.getDescriptionEn(),
                job.getCategory(),
                job.getJobType(),
                job.getShift(),
                job.getHours(),
                job.getOpenings(),
                job.getWageMin(),
                job.getWageMax(),
                job.getWagePeriod(),
                job.getLatitude(),
                job.getLongitude(),
                job.isRemote(),
                job.getMinExperienceYears(),
                job.getRequiredSkills(),
                job.getRequiredDocuments(),
                job.getPublishedAt(),
                job.getExpiresAt(),
                job.getCreatedAt());
    }
}
