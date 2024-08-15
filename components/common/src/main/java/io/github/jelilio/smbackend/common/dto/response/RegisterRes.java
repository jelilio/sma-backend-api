package io.github.jelilio.smbackend.common.dto.response;

import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record RegisterRes(
    String id,
    String name,
    String email,
    String username,
    boolean enabled,
    UserType userType
) {
  public RegisterRes(String id, String firstname, String lastname, String email, String username, boolean enabled, UserType type) {
    this(id, String.format("%s %s", checkForNull(firstname), checkForNull(lastname)), email, username, enabled, type);
  }

  private static String checkForNull(String value) {
    if(value == null) return "";
    return value;
  }

  @Override
  public String toString() {
    return "RegisterRes{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", email='" + email + '\'' +
        '}';
  }
}
