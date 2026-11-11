package com.example.studentapi.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.studentapi.dto.JobRequest;
import com.example.studentapi.dto.JobResponse;
import com.example.studentapi.dto.JobStatusChange;
import com.example.studentapi.model.Job;
import com.example.studentapi.model.Job.Status;
import com.example.studentapi.service.JobService;

// @RestController = this class handles HTTP requests and returns JSON.
@RestController
// Every URL in this class starts with this path.
@RequestMapping("/api/v1/employer/jobs")
public class EmployerJobController {

    // The controller holds no rules. It passes the work to the service.
    private final JobService jobService;

    public EmployerJobController(JobService jobService) {
        this.jobService = jobService;
    }

    // API 1: POST /api/v1/employer/jobs = post a job
    @PostMapping
    // @RequestHeader says which employer is calling (there is no login here).
    // @RequestBody turns the JSON body into a JobRequest object.
    public ResponseEntity<JobResponse> create(@RequestHeader("X-Employer-Id") UUID employerId,
            @RequestBody JobRequest request) {
        Job job = jobService.create(employerId, request);
        // 201 Created is the correct status when something new was made.
        return ResponseEntity.status(HttpStatus.CREATED).body(JobResponse.from(job));
    }

    // API 2: GET /api/v1/employer/jobs = list my jobs
    @GetMapping
    public List<JobResponse> list(@RequestHeader("X-Employer-Id") UUID employerId,
            // required = false makes ?status= optional: without it, all posts come back.
            @RequestParam(name = "status", required = false) Status status) {
        return jobService.listForEmployer(employerId, status)
                .stream()                   // go through the list one job at a time
                .map(JobResponse::from)     // convert each model object to a response
                .toList();                  // collect them into a list (a JSON array)
    }

    // API 3: GET /api/v1/employer/jobs/{id} = view one of my jobs
    // {id} is a placeholder: that part of the URL is given to the method.
    @GetMapping("/{id}")
    public JobResponse get(@RequestHeader("X-Employer-Id") UUID employerId,
            @PathVariable("id") UUID id) {
        return JobResponse.from(jobService.getOwn(employerId, id));
    }

    // API 4: PATCH /api/v1/employer/jobs/{id} = edit a job
    // PATCH means "change part of something". Only the fields sent are changed,
    // unlike PUT, which replaces everything.
    @PatchMapping("/{id}")
    public JobResponse edit(@RequestHeader("X-Employer-Id") UUID employerId,
            @PathVariable("id") UUID id,
            @RequestBody JobRequest request) {
        return JobResponse.from(jobService.edit(employerId, id, request));
    }

    // API 5: PATCH /api/v1/employer/jobs/{id}/status = pause, resume, close or repost
    @PatchMapping("/{id}/status")
    public JobResponse changeStatus(@RequestHeader("X-Employer-Id") UUID employerId,
            @PathVariable("id") UUID id,
            @RequestBody JobStatusChange change) {
        return JobResponse.from(jobService.changeStatus(employerId, id, change.action()));
    }
}
