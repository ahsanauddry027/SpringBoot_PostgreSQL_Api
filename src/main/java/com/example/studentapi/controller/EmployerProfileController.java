package com.example.studentapi.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.studentapi.dto.EmployerRequest;
import com.example.studentapi.dto.EmployerResponse;
import com.example.studentapi.model.Employer;
import com.example.studentapi.service.EmployerService;

// @RestController = "this class handles HTTP requests, and whatever its methods
// return is written to the response as JSON".
@RestController
// Every URL in this class starts with this path.
@RequestMapping("/api/v1/employer/profile")
public class EmployerProfileController {

    // The controller holds no rules. It passes the work to the service.
    private final EmployerService employerService;

    // Spring passes in the EmployerService bean it created at startup.
    public EmployerProfileController(EmployerService employerService) {
        this.employerService = employerService;
    }

    // API 1: POST /api/v1/employer/profile → create the business profile
    // @PostMapping = this method answers POST requests. POST is the method for
    // creating.
    @PostMapping
    // @RequestBody = turn the JSON in the request body into an EmployerRequest
    // object.
    // ResponseEntity lets us choose the status code ourselves.
    public ResponseEntity<EmployerResponse> create(@RequestBody EmployerRequest request) {
        Employer employer = employerService.create(request);
        // 201 Created is the correct status when something new was made (plain 200
        // means "OK").
        return ResponseEntity.status(HttpStatus.CREATED).body(EmployerResponse.from(employer));
    }

    // API 2: GET /api/v1/employer/profile → view my profile
    @GetMapping
    // @RequestHeader reads the X-Employer-Id header and converts it to a UUID.
    // There is no login in this project, so this header says which employer is
    // calling.
    public EmployerResponse get(@RequestHeader("X-Employer-Id") UUID employerId) {
        // Returning the object directly gives status 200 with the object as JSON.
        return EmployerResponse.from(employerService.getById(employerId));
    }

    // API 3: PUT /api/v1/employer/profile → edit my profile
    // PUT is the method for replacing something that already exists.
    @PutMapping
    public EmployerResponse update(@RequestHeader("X-Employer-Id") UUID employerId,
            @RequestBody EmployerRequest request) {
        return EmployerResponse.from(employerService.update(employerId, request));
    }

    // API 4: POST /api/v1/employer/profile/document → upload the verification
    // document
    // path adds "/document" to the class's URL.
    // consumes says this endpoint accepts a file upload (multipart form), not JSON.
    @PostMapping(path = "/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // @RequestParam("file") picks the form field named "file"; MultipartFile is the
    // uploaded file.
    public EmployerResponse uploadDocument(@RequestHeader("X-Employer-Id") UUID employerId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return EmployerResponse.from(employerService.uploadDocument(employerId, file));
    }
}