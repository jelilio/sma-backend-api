package io.github.jelilio.smbackend.usermanager.repository;

import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.github.jelilio.smbackend.usermanager.entity.User;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
  public Uni<User> findByEmail(String email) {
    return find("email = ?1", email).firstResult();
  }

  public Uni<User> findByUsernameOrEmail(String usernameOrEmail) {
    return find("email = ?1 or username = ?1", usernameOrEmail).firstResult();
  }

  public Uni<Long> countByEmail(String email) {
    return count("email = ?1", email);
  }

  public Uni<Long> countByEmailNotId(String id, String email) {
    return count("id != ?1 and email = ?2", UUID.fromString(id), email);
  }

  public Uni<User> findByOidcId(String id) {
    return find("oidcId = ?1", UUID.fromString(id)).firstResult();
  }

  public Uni<Long> countByIdNumber(String idNumber) {
    return count("idNumber = ?1", idNumber);
  }

  public Uni<Long> countByIdNumberButNotId(String id, String idNumber) {
    return count("id != ?1 and idNumber = ?2", UUID.fromString(id), idNumber);
  }

  public Uni<Long> countByUsername(String username) {
    return count("username = ?1", username);
  }

  public Uni<Long> countByUsernameButNotId(String id, String username) {
    return count("id != ?1 and username = ?2", UUID.fromString(id), username);
  }

  public Uni<User> findById(String id) {
    return find("id = ?1", UUID.fromString(id)).firstResult();
  }

  public PanacheQuery<User> findAllByCreatedAfterDate(Instant startDate) {
    return find("createdDate >= ?1", startDate);
  }

  public PanacheQuery<User> findAllByTypeAndCreatedAfterDate(UserType type, Instant startDate) {
    return User.find("type = ?1 and createdDate >= ?2", type, startDate);
  }

  public PanacheQuery<User> findAllByType(UserType type) {
    return User.find("type = ?1", Sort.descending("createdDate"), type);
  }
}
