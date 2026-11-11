package com.example.studentapi.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.studentapi.dto.EmployerResponse;
import com.example.studentapi.dto.VerificationDecision;
import com.example.studentapi.model.Employer.VerificationStatus;
import com.example.studentapi.service.EmployerService;

// A second controller for the admin's side, kept apart because a different role uses it.
// It uses the same service, which is why the rules live there and not in a controller.
@RestController
@RequestMapping("/api/v1/admin/employers")
public class AdminEmployerController {

    private final EmployerService employerService;

    public AdminEmployerController(EmployerService employerService) {
        this.employerService = employerService;
    }

    // API 5: GET /api/v1/admin/employers?status=PENDING → the verification queue
    @GetMapping
    public List<EmployerResponse> queue(
            // @RequestParam reads the part after "?" in the URL.
            // defaultValue is used when the client leaves ?status= out.
            @RequestParam(name = "status", defaultValue = "PENDING") VerificationStatus status) {
        return employerService.findByStatus(status)
                .stream() // go through the list one employer at a time
                .map(EmployerResponse::from) // convert each model object to a response
                .toList(); // collect them back into a list (a JSON array)
    }

    // API 6: POST /api/v1/admin/employers/{id}/verification → approve or reject
    // {id} is a placeholder: whatever is in that part of the URL is given to the
    // method.
    @PostMapping("/{id}/verification")
    // @PathVariable takes the {id} from the URL and converts it to a UUID.
    public EmployerResponse decide(@PathVariable("id") UUID id,
            @RequestBody VerificationDecision decision) {
        return EmployerResponse.from(employerService.decide(id, decision));
    }
}