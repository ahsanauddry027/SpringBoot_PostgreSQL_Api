package com.example.studentapi.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.studentapi.dto.EmployerRequest;
import com.example.studentapi.dto.VerificationDecision;
import com.example.studentapi.model.Employer;
import com.example.studentapi.model.Employer.VerificationStatus;
import com.example.studentapi.repository.EmployerRepository;

// @Service tells Spring: "create one object of this class at startup and keep it ready".
// Such a Spring-managed object is called a bean. The business rules live here so that
// controllers stay short and the same rules can be reused by any controller.
@Service
public class EmployerService {

    // The file types we accept for a trade licence or national ID.
    // "static final" = one shared constant that never changes.
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "application/pdf");

    // "final" means these are set once in the constructor and never replaced.
    private final EmployerRepository employerRepository;
    private final Path filesDir;

    // Constructor injection: Spring sees that this class needs an
    // EmployerRepository
    // and passes one in. We never write "new EmployerRepository()" ourselves.
    // @Value reads kaaj.files.dir from application.properties; the part after ":"
    // is
    // the fallback used if that line is missing.
    public EmployerService(EmployerRepository employerRepository,
            @Value("${kaaj.files.dir:./data/uploads}") String filesDir) {
        this.employerRepository = employerRepository;
        // Turn "./data/uploads" into a full path such as
        // C:/.../studentapi/data/uploads.
        this.filesDir = Path.of(filesDir).toAbsolutePath();
    }

    // Create a new business profile.
    public Employer create(EmployerRequest request) {
        Employer employer = new Employer(); // an empty object, status UNVERIFIED
        copyFields(request, employer); // check the input and fill the object
        return employerRepository.save(employer); // INSERT INTO employer ...; returns it with its new id
    }

    // Load one employer, or answer 404 if the id does not exist.
    public Employer getById(UUID id) {
        // findById returns an Optional: a box that may be empty.
        // orElseThrow opens the box, or throws our error when it is empty.
        return employerRepository.findById(id)
                // ResponseStatusException makes Spring send this HTTP status and message to the
                // client.
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found"));
    }

    // Edit an existing profile.
    public Employer update(UUID id, EmployerRequest request) {
        Employer employer = getById(id); // 404 if it does not exist
        copyFields(request, employer); // overwrite the editable fields
        return employerRepository.save(employer); // the object has an id, so this is an UPDATE
    }

    // Save the uploaded document and put the employer in the admin's queue.
    // "throws IOException" because writing a file to disk can fail.
    public Employer uploadDocument(UUID id, MultipartFile file) throws IOException {
        Employer employer = getById(id);

        // Rule: an approved employer does not need to be checked again. 409 = Conflict.
        if (employer.getVerificationStatus() == VerificationStatus.VERIFIED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This employer is already verified");
        }
        // Rule: the file must exist and be a JPEG, PNG or PDF. 400 = Bad Request.
        if (file.isEmpty() || !ALLOWED_TYPES.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload a JPEG, PNG or PDF file");
        }

        // A random name, so two uploads never overwrite each other and nobody can guess
        // a file's name.
        String key = UUID.randomUUID().toString();
        // Make the uploads folder if it is not there yet.
        Files.createDirectories(filesDir);
        // try-with-resources: the stream is closed automatically, even if copying
        // fails.
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, filesDir.resolve(key)); // write the bytes to data/uploads/<key>
        }

        employer.setDocumentKey(key); // remember which file belongs to this employer
        employer.setVerificationStatus(VerificationStatus.PENDING); // now waiting for an admin
        employer.setVerificationNote(null); // clear an old rejection reason
        return employerRepository.save(employer);
    }

    // The admin's queue: every employer with the given status.
    public List<Employer> findByStatus(VerificationStatus status) {
        return employerRepository.findByVerificationStatus(status);
    }

    // The admin approves or rejects.
    public Employer decide(UUID id, VerificationDecision decision) {
        Employer employer = getById(id);

        // Rule: only someone waiting in the queue can be decided on.
        if (employer.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only a pending employer can be approved or rejected");
        }

        // equalsIgnoreCase accepts "approve" and "APPROVE", and is safe when decision
        // is null.
        if ("APPROVE".equalsIgnoreCase(decision.decision())) {
            employer.setVerificationStatus(VerificationStatus.VERIFIED);
            employer.setVerifiedAt(Instant.now()); // the moment of approval
            employer.setVerificationNote(null);
        } else if ("REJECT".equalsIgnoreCase(decision.decision())) {
            // Rule: the employer must be told why, so a reason is required.
            if (decision.reason() == null || decision.reason().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A reason is required when rejecting");
            }
            employer.setVerificationStatus(VerificationStatus.REJECTED);
            employer.setVerificationNote(decision.reason());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "decision must be APPROVE or REJECT");
        }
        return employerRepository.save(employer);
    }

    // "private" = only this class uses it. Shared by create() and update() so the
    // checks are written once.
    private void copyFields(EmployerRequest request, Employer employer) {
        // Rule: a profile needs at least a name and a business type.
        if (request.name() == null || request.name().isBlank() || request.kind() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name and kind are required");
        }
        employer.setName(request.name());
        employer.setKind(request.kind());
        employer.setAddress(request.address());
        employer.setLatitude(request.latitude());
        employer.setLongitude(request.longitude());
        employer.setContactPerson(request.contactPerson());
    }
}