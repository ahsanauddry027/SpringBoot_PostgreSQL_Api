// The folder (package) this file lives in. It must match the folder path.
package com.example.studentapi.model;

// Instant = one exact moment in time (date + time, in UTC).
import java.time.Instant;
// UUID = a long random id such as 0193c8b0-1a2b-7c3d-8e4f-5a6b7c8d9e0f.
import java.util.UUID;

// JPA annotations. JPA is the standard that maps Java classes to database tables.
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// @Entity tells Hibernate: "this class is a database table; each object is one row".
@Entity
// @Table sets the table's name. Without it, Hibernate would use the class name.
@Table(name = "employer")
public class Employer {

    // An enum is a fixed list of allowed values. Using one stops typos like
    // "COMPNY".
    public enum Kind {
        HOUSEHOLD, SME, COMPANY, AGENCY, NGO
    }

    // The four stages of verification, in the order an employer moves through them.
    public enum VerificationStatus {
        UNVERIFIED, PENDING, VERIFIED, REJECTED
    }

    // @Id marks the primary key: the column that identifies one row.
    @Id
    // @GeneratedValue means we never set the id ourselves; a random UUID is created
    // on save.
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // nullable = false adds NOT NULL to the column, so the database refuses a row
    // without a name.
    @Column(nullable = false)
    private String name;

    // EnumType.STRING stores the word ("SME") in the table. The default stores a
    // number (1),
    // which breaks if someone later reorders the enum.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Kind kind;

    // Fields with no annotation become ordinary columns that may be empty (NULL).
    private String address;

    // Double (capital D) can be null, for an employer who has not set a map pin
    // yet.
    private Double latitude;
    private Double longitude;

    // Becomes the column contact_person: Spring turns camelCase into snake_case.
    private String contactPerson;

    // The random file name of the uploaded document. We store the name, never the
    // file itself.
    private String documentKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // "= UNVERIFIED" is the starting value for every new employer.
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    // The reason the employer sees when an admin rejects them.
    private String verificationNote;

    // When the admin approved. Stays null until then.
    private Instant verifiedAt;

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getDocumentKey() {
        return documentKey;
    }

    public void setDocumentKey(String documentKey) {
        this.documentKey = documentKey;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getVerificationNote() {
        return verificationNote;
    }

    public void setVerificationNote(String verificationNote) {
        this.verificationNote = verificationNote;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

}