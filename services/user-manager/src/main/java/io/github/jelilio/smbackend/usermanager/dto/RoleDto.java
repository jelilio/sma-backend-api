package io.github.jelilio.smbackend.usermanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record RoleDto(
    String name,
    String description,
    boolean composite,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Set<RoleDto> compositeRoles
) {
  public RoleDto(String name, String description, boolean composite) {
    this(name, description, composite, new HashSet<>());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RoleDto roleDto = (RoleDto) o;
    return name.equals(roleDto.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
