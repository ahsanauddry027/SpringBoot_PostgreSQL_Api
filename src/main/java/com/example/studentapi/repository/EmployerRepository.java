package com.example.studentapi.repository;

import java.util.List;
import java.util.UUID;

// JpaRepository is Spring Data's ready-made set of database methods.
import org.springframework.data.jpa.repository.JpaRepository;

// We import our own classes from the model package because this file is in another package.
import com.example.studentapi.model.Employer;
import com.example.studentapi.model.Employer.VerificationStatus;

// This is an interface with no code inside. Spring writes the implementation at startup.
// JpaRepository<Employer, UUID> means "a repository for Employer rows whose id is a UUID".
// We get save(), findById(), findAll(), deleteById() and more for free.
public interface EmployerRepository extends JpaRepository<Employer, UUID> {

    // Spring reads the method NAME and builds the SQL from it:
    // SELECT * FROM employer WHERE verification_status = ?
    // Used by the admin's verification queue.
    List<Employer> findByVerificationStatus(VerificationStatus status);
}