package io.github.jelilio.smbackend.usermanager.model;

import java.util.List;
import java.util.Set;

import static io.github.jelilio.smbackend.usermanager.utils.StringUtils.splitName;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

public record Register(
    String email,
    String firstName,
    String lastName,
    boolean enabled,
    boolean emailVerified,
    List<Credential> credentials
//    Map<Object, Object> attributes
) {
  public  Register(String email, String name, String password) {
    this(email, splitName(name).first(), splitName(name).second(), false, true, List.of(new Credential(password)));
  }

  public Register(String email, String name) {
    this(email, splitName(name).first(), splitName(name).second(), false, true, emptyList());
  }

  public  Register(String email, String name, boolean enabled, boolean emailVerified, String password) {
    this(email, splitName(name).first(), splitName(name).second(), enabled, emailVerified, List.of(new Credential(password)));
  }

  public String getUsername() {
    return email;
  }

  public boolean getEnabled() {
    return enabled;
  }

  public boolean getEmailVerified() {
    return true;
  }

  public Set<String> getRealmRoles() {
    return emptySet();
  }

  @Override
  public String toString() {
    return "Register{" +
        "email='" + email + '\'' +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", enabled=" + enabled +
        ", emailVerified=" + emailVerified +
        ", credentials=" + credentials +
        '}';
  }
}
