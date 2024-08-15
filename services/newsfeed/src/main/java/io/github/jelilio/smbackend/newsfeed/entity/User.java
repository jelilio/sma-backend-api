package io.github.jelilio.smbackend.newsfeed.entity;

import io.github.jelilio.smbackend.common.dto.response.OwnerRes;
import io.github.jelilio.smbackend.common.entity.base.AbstractAuditingEntity;
import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.github.jelilio.smbackend.common.entity.listener.AuditingEntityListener;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Cacheable
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User extends AbstractAuditingEntity {
  public static final String USER_EMAIL_PROPERTY = "email";
  public static final String USER_USERNAME_PROPERTY = "username";

  @Id
  public UUID id;

  @Column(name = "name", length = 100, nullable = false)
  public String name;

  @Email
  @Column(name = "email", unique = true, nullable = false)
  public String email;

  @Column(name = "username", unique = true, length = 100)
  public String username;

  @Column(name = "enabled")
  public boolean enabled = false;

  // for verification request
  @Column(name = "request_date")
  public Instant requestDate;

  @Column(name = "verified_date")
  public Instant verifiedDate;

  @Column(name = "suspended_date")
  public Instant suspendedDate;

  @Column(name = "id_number", unique = true, length = 100)
  public String idNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  public UserType type;

  @Column(name = "bio", length = 500)
  public String bio;

  @Column(name = "avatar_url")
  public String avatarUrl;

  @Column(name = "avatar_type")
  public String avatarType;

  @Column(name = "birth_date")
  public LocalDate birthDate;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "owner_id")
  public User owner;

  // for club
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "requester_id")
  public User requester;

  @Column(name = "verified_by")
  public String verifiedBy;

  @Column(name = "request_id_number")
  public String requestIdNumber;

  // for community and club
  @Column(name = "member_count")
  public Long memberCount;

  public static Uni<User> findByEmail(String email) {
    return User.find("email = ?1", email).firstResult();
  }

  public static Uni<User> findByUsernameOrEmail(String usernameOrEmail) {
    return User.find("email = ?1 or username = ?1", usernameOrEmail).firstResult();
  }

  public static Uni<Long> countByEmail(String email) {
    return User.count(USER_EMAIL_PROPERTY, email);
  }

  public static Uni<User> findById(String id) {
    return User.findById(UUID.fromString(id));
  }

  public static Uni<User> findByIdCommunityOrClub(String id) {
    return User.find("id = ?1 and (type = ?2 or type = ?3)", UUID.fromString(id),
        UserType.CLUB, UserType.COMMUNITY).firstResult();
  }

  public static Uni<User> findByIdNonCommunityOrClub(String id) {
    return User.find("id = ?1 and (type != ?2 or type != ?3)", UUID.fromString(id),
        UserType.CLUB, UserType.COMMUNITY).firstResult();
  }

  public static Uni<User> findByIdAndType(String id, UserType type) {
    return User.find("id = ?1 and type = ?2", UUID.fromString(id), type).firstResult();
  }

  public static PanacheQuery<User> findAllByType(UserType type) {
    return User.find("type = ?1", Sort.descending("createdDate"), type);
  }

  public static Uni<Long> countCommunities_(UserType type) {
    return User.count("type = ?1", type);
  }

  public static Uni<Long> countCommunityClubs_(String ownerId, UserType type) {
    return User.count("type = ?1 and owner.id = ?2", type, UUID.fromString(ownerId));
  }

  public static Uni<Long> countAllUser(String query, List<UserType> userTypes) {
    return User.count("SELECT count(distinct t.id) FROM User t WHERE (lower(t.name) like ?1 or lower(t.username) like ?1) and t.type in ?2", query, userTypes);
  }

  public User() {}

  public User(String id, String name, String email) {
    this.id = UUID.fromString(id);
    this.name = name;
    this.email = email;
  }

  public User(String id, String name, String email, String username) {
    this.id = UUID.fromString(id);
    this.name = name;
    this.email = email;
    this.username = username;
  }

  public User(String id, String name, String email, boolean enabled, UserType type) {
    this.id = UUID.fromString(id);
    this.name = name;
    this.email = email;
    this.type = type;
    this.enabled = enabled;
  }

  public User(UUID id, String name, String username) {
    this.id = id;
    this.name = name;
    this.username = username;
  }

  public Integer getAge() {
    if (birthDate == null) return null;

    return Period.between(birthDate, LocalDate.now())
        .getYears();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return Objects.equals(id, user.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "User{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", email='" + email + '\'' +
        ", type=" + type +
        '}';
  }

  public OwnerRes ownerRes() {
    return new OwnerRes(
        owner == null? null : owner.id.toString(),
        owner == null? null : owner.name,
        owner == null? null : owner.username,
        requester == null? null : requester.id.toString(),
        requester == null? null :requester.name,
        requester == null? null :requester.username
    );
  }
}
