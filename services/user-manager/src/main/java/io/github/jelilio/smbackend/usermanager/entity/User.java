package io.github.jelilio.smbackend.usermanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jelilio.smbackend.common.dto.UserReq;
import io.github.jelilio.smbackend.common.entity.base.AbstractAuditingEntity;
import io.github.jelilio.smbackend.common.entity.enumeration.UserStatus;
import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.github.jelilio.smbackend.common.entity.listener.AuditingEntityListener;
import io.github.jelilio.smbackend.usermanager.model.ExcelFile;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Cacheable
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User extends AbstractAuditingEntity {
  public static final String USER_EMAIL_PROPERTY = "email";
  public static final String USER_USERNAME_PROPERTY = "username";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Schema(readOnly = true)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public UUID id;

  @Column(name = "oidc_id", nullable = false, unique = true)
  public UUID oidcId;

  @Column(name = "name", length = 100, nullable = false)
  public String name;

  @Email
  @Column(name = "email", unique = true, nullable = false)
  public String email;

  @Column(name = "username", unique = true, length = 100)
  public String username;

  @Column(name = "enabled")
  public boolean enabled = false;

  @Column(name = "id_number", unique = true, length = 100)
  public String idNumber;

  @Column(name = "birth_date")
  public LocalDate birthDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  public UserType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  public UserStatus status = UserStatus.ON_QUEUE;

  @Column(name = "claimed_date")
  public Instant claimedDate;

  @Column(name = "avatar_url")
  public String avatarUrl;

  @Column(name = "avatar_type")
  public String avatarType;

  // STUDENT
  @ManyToOne
  @JoinColumn(name = "course_id")
  public Course course;

  @Column(name = "start_year")
  public LocalDate startYear;

  @Column(name = "level")
  public Integer level;

  // ALUMNI
  @Column(name = "end_year")
  public LocalDate endYear;

  // STAFF
  @ManyToOne
  @JoinColumn(name = "school_id")
  public School school;

  // for verification request
  @Column(name = "request_date")
  public Instant requestDate;

  @Column(name = "request_id_number")
  public String requestIdNumber;

  @Column(name = "verified_date")
  public Instant verifiedDate;

  @Column(name = "suspended_date")
  public Instant suspendedDate;

  @Column(name = "verified_by")
  public String verifiedBy;

  public User() {}

  public User(String name, String email, UserType type) {
    this.name = name;
    this.email = email;
    this.enabled = false;
    this.type = type;
    this.status = UserStatus.PENDING;
  }

  public User(String id, String name, String email) {
    this.oidcId = UUID.fromString(id);
    this.name = name;
    this.email = email;
    this.status = UserStatus.DONE;
  }

  public User(String id, String name, String email, UserType type) {
    this.oidcId = UUID.fromString(id);
    this.name = name;
    this.email = email;
    this.type = type;
    this.status = UserStatus.DONE;
  }

  public User(String id, String name, String email, UserType type, boolean enabled) {
    this.oidcId = UUID.fromString(id);
    this.name = name;
    this.email = email;
    this.type = type;
    this.status = UserStatus.DONE;
    this.enabled = enabled;
  }

  public User(String id, String name, String email, String username, UserType type, boolean enabled) {
    this.oidcId = UUID.fromString(id);
    this.name = name;
    this.email = email;
    this.type = type;
    this.status = UserStatus.DONE;
    this.enabled = enabled;
    this.username = username;
  }

  public User(ExcelFile line, Course course) {
    this.oidcId = UUID.randomUUID(); // temporary-id
    this.name = line.name;
    this.email = line.email;
    this.enabled = line.enabled;
    this.type = line.type != null? line.type : UserType.NORMAL;
    this.birthDate = line.birthDate;
    this.idNumber = line.idNumber;
    this.course = course;
  }

  public User(String id, ExcelFile line, Course course) {
    this.oidcId = UUID.fromString(id);
    this.name = line.name;
    this.email = line.email;
    this.enabled = line.enabled;
    this.type = line.type != null? line.type : UserType.NORMAL;
    this.birthDate = line.birthDate;
    this.idNumber = line.idNumber;
    this.course = course;
    this.status = UserStatus.DONE;
  }

  public User(UserReq req, UserType type, Course course) {
    this.oidcId = UUID.randomUUID(); // temporary-id
    this.name = req.name();
    this.email = req.email();
    this.enabled = true;
    this.type = type;
    this.birthDate = req.birthDate();
    this.idNumber = req.idNumber();
    this.course = course;
    this.status = UserStatus.ON_QUEUE;
  }

  public User(String oidcId, UserReq req, UserType type) {
    this.oidcId = UUID.fromString(oidcId); // temporary-id
    this.name = req.name();
    this.email = req.email();
    this.enabled = false;
    this.type = type;
    this.birthDate = req.birthDate();
    this.idNumber = req.idNumber();
    this.status = UserStatus.ON_QUEUE;
  }

  // to create sStudent
  public User(String oidcId, UserReq req, UserType type, Course course) {
    this(oidcId, req, type);
    this.course = course;
  }

  // to create Staff
  public User(String oidcId, UserReq req, UserType type, School school) {
    this(oidcId, req, type);
    this.school = school;
  }

  public boolean isClaimed() {
    return claimedDate != null;
  }

  public boolean isVerified() {
    return verifiedDate != null;
  }

  public boolean isRequestVerification() {
    return requestDate != null;
  }
}
