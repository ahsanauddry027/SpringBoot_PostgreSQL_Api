package com.example.studentapi.model;

// Instant = one exact moment in time; ChronoUnit lets us add "30 days" to it.
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Hibernate helpers used to store a Java List as a PostgreSQL array column.
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// @Entity = this class is a database table; each Job object is one row of it.
@Entity
@Table(name = "job")
public class Job {

    // Fixed lists of allowed values. An enum stops typos such as "FULLTIME".
    public enum JobType { FULL_TIME, PART_TIME, DAILY }

    public enum Shift { DAY, NIGHT, ROTATING, PART_TIME, FLEXIBLE }

    // The wage is an amount in taka for one of these periods.
    public enum WagePeriod { HOUR, DAY, WEEK, MONTH, PIECE }

    // The life of a post: DRAFT until it may go live, PUBLISHED when job seekers
    // can see it, PAUSED when the employer hides it for a while, CLOSED when done.
    public enum Status { DRAFT, PUBLISHED, PAUSED, CLOSED }

    // A post closes by itself this many days after it is published.
    public static final int EXPIRY_DAYS = 30;

    // Primary key: a random UUID created when the row is first saved.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // @ManyToOne = many jobs belong to one employer. This is the link between
    // the two features: every post has an owner.
    // optional = false means a job can never exist without an employer.
    @ManyToOne(optional = false)
    // @JoinColumn names the foreign key column that stores the employer's id.
    @JoinColumn(name = "employer_id", nullable = false)
    private Employer employer;

    // Titles and descriptions are kept in Bangla and in English.
    @Column(nullable = false)
    private String titleBn;

    @Column(nullable = false)
    private String titleEn;

    // length = 4000 because a description is longer than the default 255 letters.
    @Column(nullable = false, length = 4000)
    private String descriptionBn;

    @Column(nullable = false, length = 4000)
    private String descriptionEn;

    // For example "Driver" or "Garments".
    private String category;

    // EnumType.STRING stores the word ("FULL_TIME") instead of a number.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType jobType = JobType.FULL_TIME;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Shift shift = Shift.DAY;

    // Working hours as text, for example "08:00-18:00".
    private String hours;

    // How many people the employer wants to hire.
    @Column(nullable = false)
    private Integer openings = 1;

    // The wage range in whole taka. A post cannot be saved without it.
    @Column(nullable = false)
    private Integer wageMin;

    @Column(nullable = false)
    private Integer wageMax;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WagePeriod wagePeriod;

    // The map pin of the workplace. Both stay empty for a remote job.
    private Double latitude;
    private Double longitude;

    // true = the work can be done from anywhere, so no map pin is needed.
    @Column(nullable = false)
    private boolean remote = false;

    // Requirements: years of experience, skills and documents such as a licence.
    private Integer minExperienceYears;

    // @JdbcTypeCode(SqlTypes.ARRAY) stores the whole list in ONE array column,
    // so we do not need a second table just for skills.
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> requiredSkills = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> requiredDocuments = new ArrayList<>();

    // Every new post starts as a DRAFT.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.DRAFT;

    // When the post went live, and when it will close by itself.
    private Instant publishedAt;
    private Instant expiresAt;

    // When the row was created. Used to show the newest post first.
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    // Makes the post live and starts its 30-day clock. The rule is written once
    // here so creating, reposting and approving an employer all behave the same.
    public void publish() {
        this.status = Status.PUBLISHED;
        this.publishedAt = Instant.now();
        this.expiresAt = this.publishedAt.plus(EXPIRY_DAYS, ChronoUnit.DAYS);
    }

    // Getters and setters: the fields are private, so other classes read and
    // change them through these methods.

    public UUID getId() {
        return id;
    }

    public Employer getEmployer() {
        return employer;
    }

    public void setEmployer(Employer employer) {
        this.employer = employer;
    }

    public String getTitleBn() {
        return titleBn;
    }

    public void setTitleBn(String titleBn) {
        this.titleBn = titleBn;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public String getDescriptionBn() {
        return descriptionBn;
    }

    public void setDescriptionBn(String descriptionBn) {
        this.descriptionBn = descriptionBn;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public Integer getOpenings() {
        return openings;
    }

    public void setOpenings(Integer openings) {
        this.openings = openings;
    }

    public Integer getWageMin() {
        return wageMin;
    }

    public void setWageMin(Integer wageMin) {
        this.wageMin = wageMin;
    }

    public Integer getWageMax() {
        return wageMax;
    }

    public void setWageMax(Integer wageMax) {
        this.wageMax = wageMax;
    }

    public WagePeriod getWagePeriod() {
        return wagePeriod;
    }

    public void setWagePeriod(WagePeriod wagePeriod) {
        this.wagePeriod = wagePeriod;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public Integer getMinExperienceYears() {
        return minExperienceYears;
    }

    public void setMinExperienceYears(Integer minExperienceYears) {
        this.minExperienceYears = minExperienceYears;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public List<String> getRequiredDocuments() {
        return requiredDocuments;
    }

    public void setRequiredDocuments(List<String> requiredDocuments) {
        this.requiredDocuments = requiredDocuments;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
