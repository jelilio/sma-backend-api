package io.github.jelilio.smbackend.usermanager.model;

import java.util.*;

public class KUser {
  public String id;
  public String email;
  public String username;
  public Boolean enabled;
  public Boolean emailVerified;
  public String firstName;
  public String lastName;
  public Set<String> roles = new HashSet<>();
  public Map<Object, Object> attributes = new HashMap<>();

  @Override
  public String toString() {
    return "KUser{" +
        "id='" + id + '\'' +
        ", email='" + email + '\'' +
        ", username='" + username + '\'' +
        ", enabled=" + enabled +
        ", emailVerified=" + emailVerified +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", roles=" + roles +
        ", attributes=" + attributes +
        '}';
  }
}