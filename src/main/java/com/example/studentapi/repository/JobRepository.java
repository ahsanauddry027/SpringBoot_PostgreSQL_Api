package com.example.studentapi.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.studentapi.model.Job;
import com.example.studentapi.model.Job.Status;

// An interface with no code inside: Spring writes the implementation at startup.
// JpaRepository<Job, UUID> gives us save(), findById(), findAll() and more for
// Job rows whose id is a UUID.
public interface JobRepository extends JpaRepository<Job, UUID> {

    // Spring builds the SQL from the method NAME:
    // SELECT * FROM job WHERE employer_id = ? ORDER BY created_at DESC
    // "EmployerId" means "the id of the job's employer field".
    List<Job> findByEmployerIdOrderByCreatedAtDesc(UUID employerId);

    // The same list, narrowed to one status (for example only PUBLISHED posts).
    List<Job> findByEmployerIdAndStatusOrderByCreatedAtDesc(UUID employerId, Status status);

    // Used when an admin approves an employer: find their waiting drafts.
    List<Job> findByEmployerIdAndStatus(UUID employerId, Status status);
}
