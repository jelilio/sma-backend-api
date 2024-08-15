package io.github.jelilio.smbackend.userdirectory.entity;

import io.github.jelilio.smbackend.common.entity.base.AbstractAuditingEntity;
import io.github.jelilio.smbackend.common.entity.listener.AuditingEntityListener;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;

import java.time.Instant;
import java.time.LocalDate;
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

  @Column(name = "verified_date")
  public Instant verifiedDate;

  @Column(name = "id_number", unique = true, length = 100)
  public String idNumber;

  @Column(name = "birth_date")
  public LocalDate birthDate;

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

  public User() {}

  public User(String id, String name, String email) {
    this.id = UUID.fromString(id);
    this.name = name;
    this.email = email;
  }

  public User(UUID id, String name) {
    this.id = id;
    this.name = name;
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

  public boolean isVerified() {
    return verifiedDate != null;
  }

  @Override
  public String toString() {
    return "User{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", email='" + email + '\'' +
        '}';
  }
}
