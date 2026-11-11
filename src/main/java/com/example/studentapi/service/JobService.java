package com.example.studentapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.studentapi.dto.JobRequest;
import com.example.studentapi.model.Employer;
import com.example.studentapi.model.Employer.VerificationStatus;
import com.example.studentapi.model.Job;
import com.example.studentapi.model.Job.Status;
import com.example.studentapi.repository.JobRepository;

// @Service = Spring creates one object of this class at startup (a "bean").
// All the rules for job posts live here, so the controller stays short.
@Service
public class JobService {

    private final JobRepository jobRepository;
    // We reuse EmployerService to load the employer, so the "404 Employer not
    // found" rule is written in one place only.
    private final EmployerService employerService;

    // Constructor injection: Spring passes in the two beans this class needs.
    public JobService(JobRepository jobRepository, EmployerService employerService) {
        this.jobRepository = jobRepository;
        this.employerService = employerService;
    }

    // Post a new job for this employer.
    public Job create(UUID employerId, JobRequest request) {
        Employer employer = employerService.getById(employerId); // 404 if unknown
        Job job = new Job();                 // a new post, status DRAFT
        job.setEmployer(employer);           // the employer who owns it
        copyFields(request, job);            // fill it from the JSON
        validate(job);                       // check the rules (wage, title, location)

        // Rule: only a verified employer can publish. Everyone else's post is
        // saved as a DRAFT and goes live when an admin approves the employer.
        if (employer.getVerificationStatus() == VerificationStatus.VERIFIED) {
            job.publish();
        }
        return jobRepository.save(job);      // INSERT INTO job ...
    }

    // All of this employer's posts, newest first. The status filter is optional.
    public List<Job> listForEmployer(UUID employerId, Status status) {
        employerService.getById(employerId); // 404 if the employer does not exist
        if (status == null) {
            return jobRepository.findByEmployerIdOrderByCreatedAtDesc(employerId);
        }
        return jobRepository.findByEmployerIdAndStatusOrderByCreatedAtDesc(employerId, status);
    }

    // Load one job and make sure it belongs to the employer who is asking.
    public Job getOwn(UUID employerId, UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        // Ownership check: an employer may only see and change their own posts.
        // 403 = Forbidden.
        if (!job.getEmployer().getId().equals(employerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This job belongs to another employer");
        }
        return job;
    }

    // Edit a post. Only the fields present in the JSON are changed.
    public Job edit(UUID employerId, UUID jobId, JobRequest request) {
        Job job = getOwn(employerId, jobId);
        // Rule: a closed post is finished. It must be reposted before editing.
        if (job.getStatus() == Status.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A closed job cannot be edited. Repost it first");
        }
        copyFields(request, job);
        validate(job);                       // the edited post must still obey the rules
        return jobRepository.save(job);      // the job has an id, so this is an UPDATE
    }

    // Pause, resume, close or repost a post.
    public Job changeStatus(UUID employerId, UUID jobId, String action) {
        Job job = getOwn(employerId, jobId);
        // Accept "pause" and "PAUSE"; a missing action becomes "" and is refused below.
        String wanted = action == null ? "" : action.trim().toUpperCase();

        // Each action is only allowed from certain statuses.
        switch (wanted) {
            case "PAUSE" -> {                // hide a live post for a while
                requireStatus(job, wanted, Status.PUBLISHED);
                job.setStatus(Status.PAUSED);
            }
            case "RESUME" -> {               // show a paused post again
                requireStatus(job, wanted, Status.PAUSED);
                job.setStatus(Status.PUBLISHED);
            }
            case "CLOSE" -> {                // the job is filled or no longer needed
                requireStatus(job, wanted, Status.DRAFT, Status.PUBLISHED, Status.PAUSED);
                job.setStatus(Status.CLOSED);
            }
            case "REPOST" -> {               // open a closed post again for 30 more days
                requireStatus(job, wanted, Status.CLOSED);
                if (job.getEmployer().getVerificationStatus() == VerificationStatus.VERIFIED) {
                    job.publish();
                } else {
                    job.setStatus(Status.DRAFT);
                }
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "action must be PAUSE, RESUME, CLOSE or REPOST");
        }
        return jobRepository.save(job);
    }

    // Refuses the action with 409 Conflict unless the job is in an allowed status.
    // "Status... allowed" means the caller may pass one status or several.
    private void requireStatus(Job job, String action, Status... allowed) {
        for (Status status : allowed) {
            if (job.getStatus() == status) {
                return;                      // allowed: carry on
            }
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Cannot " + action + " a job that is " + job.getStatus());
    }

    // Copies each field that is present in the request onto the job.
    // null means "not sent", so the old value stays. That is what makes PATCH
    // (a partial edit) work with the same code as creating.
    private void copyFields(JobRequest request, Job job) {
        if (request.titleBn() != null) job.setTitleBn(request.titleBn());
        if (request.titleEn() != null) job.setTitleEn(request.titleEn());
        if (request.descriptionBn() != null) job.setDescriptionBn(request.descriptionBn());
        if (request.descriptionEn() != null) job.setDescriptionEn(request.descriptionEn());
        if (request.category() != null) job.setCategory(request.category());
        if (request.jobType() != null) job.setJobType(request.jobType());
        if (request.shift() != null) job.setShift(request.shift());
        if (request.hours() != null) job.setHours(request.hours());
        if (request.openings() != null) job.setOpenings(request.openings());
        if (request.wageMin() != null) job.setWageMin(request.wageMin());
        if (request.wageMax() != null) job.setWageMax(request.wageMax());
        if (request.wagePeriod() != null) job.setWagePeriod(request.wagePeriod());
        if (request.latitude() != null) job.setLatitude(request.latitude());
        if (request.longitude() != null) job.setLongitude(request.longitude());
        if (request.remote() != null) job.setRemote(request.remote());
        if (request.minExperienceYears() != null) job.setMinExperienceYears(request.minExperienceYears());
        if (request.requiredSkills() != null) job.setRequiredSkills(request.requiredSkills());
        if (request.requiredDocuments() != null) job.setRequiredDocuments(request.requiredDocuments());
    }

    // The business rules every post must obey. They are Java code, not hints in
    // a form, so no client can skip them. 400 = Bad Request.
    private void validate(Job job) {
        // Rule: a title in at least one language. The other language is filled
        // with the same text so both columns always have a value.
        if (isBlank(job.getTitleBn()) && isBlank(job.getTitleEn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A job title is required");
        }
        if (isBlank(job.getTitleBn())) job.setTitleBn(job.getTitleEn());
        if (isBlank(job.getTitleEn())) job.setTitleEn(job.getTitleBn());

        // Rule: a description in at least one language.
        if (isBlank(job.getDescriptionBn()) && isBlank(job.getDescriptionEn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A job description is required");
        }
        if (isBlank(job.getDescriptionBn())) job.setDescriptionBn(job.getDescriptionEn());
        if (isBlank(job.getDescriptionEn())) job.setDescriptionEn(job.getDescriptionBn());

        // Rule: at least one opening.
        if (job.getOpenings() == null || job.getOpenings() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "openings must be 1 or more");
        }

        // Rule: wages up front. A post cannot be saved without a wage.
        if (job.getWageMin() == null || job.getWageMin() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A wage or wage range is required");
        }
        // A single wage is stored as a range with the same two numbers.
        if (job.getWageMax() == null) job.setWageMax(job.getWageMin());
        if (job.getWageMin() > job.getWageMax()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "wageMin cannot be more than wageMax");
        }
        if (job.getWagePeriod() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "wagePeriod is required: HOUR, DAY, WEEK, MONTH or PIECE");
        }

        // Rule: a job has a place, unless it is remote.
        if (!job.isRemote() && (job.getLatitude() == null || job.getLongitude() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Give a location (latitude and longitude) or mark the job as remote");
        }
    }

    // true when the text is missing or only spaces.
    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
