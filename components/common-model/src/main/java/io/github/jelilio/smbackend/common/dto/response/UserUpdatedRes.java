package io.github.jelilio.smbackend.common.dto.response;

import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class UserUpdatedRes {
  public String id;
  public String name;
  public String email;
  public String username;
  public boolean enabled;
  public UserType userType;
  public String imageUrl;
  public String imageType;
}
