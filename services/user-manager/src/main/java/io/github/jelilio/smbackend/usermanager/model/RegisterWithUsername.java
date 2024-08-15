package io.github.jelilio.smbackend.usermanager.model;

import java.util.List;
import java.util.Set;

import static io.github.jelilio.smbackend.usermanager.utils.StringUtils.splitName;
import static java.util.Collections.emptySet;

public record RegisterWithUsername(
    String email,
    String username,
    String firstName,
    String lastName,
    boolean enabled,
    boolean emailVerified,
    List<Credential> credentials
) {
  public RegisterWithUsername(String email, String username, String name, boolean enabled, boolean emailVerified, String password) {
    this(email, username, splitName(name).first(), splitName(name).second(), enabled, emailVerified, List.of(new Credential(password)));
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
    return "RegisterWithUsername{" +
        "email='" + email + '\'' +
        ", username='" + username + '\'' +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", enabled=" + enabled +
        ", emailVerified=" + emailVerified +
        ", credentials=" + credentials +
        '}';
  }
}
