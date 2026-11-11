package com.example.studentapi.dto;

import java.util.List;

import com.example.studentapi.model.Job.JobType;
import com.example.studentapi.model.Job.Shift;
import com.example.studentapi.model.Job.WagePeriod;

// The JSON body an employer SENDS to post a job or to edit one.
// It lists only what an employer may set. The status, the owner and the dates
// are not here, so a client cannot publish a post or change its owner by hand.
//
// Every field uses a type that can be null (Integer, Boolean, not int, boolean).
// When editing, a field left out of the JSON arrives as null and means
// "keep the old value".
public record JobRequest(
        String titleBn,                 // job title in Bangla
        String titleEn,                 // job title in English (one of the two is required)
        String descriptionBn,
        String descriptionEn,           // one of the two descriptions is required
        String category,                // for example "Driver"
        JobType jobType,                // FULL_TIME, PART_TIME or DAILY
        Shift shift,                    // DAY, NIGHT, ROTATING, PART_TIME or FLEXIBLE
        String hours,                   // for example "08:00-18:00"
        Integer openings,               // how many people are needed
        Integer wageMin,                // lowest wage in taka (required)
        Integer wageMax,                // highest wage in taka (wageMin is used if left out)
        WagePeriod wagePeriod,          // HOUR, DAY, WEEK, MONTH or PIECE (required)
        Double latitude,                // map pin of the workplace
        Double longitude,
        Boolean remote,                 // true = no map pin needed
        Integer minExperienceYears,
        List<String> requiredSkills,    // for example ["Pickup driving"]
        List<String> requiredDocuments) {   // for example ["Driving licence"]
}
