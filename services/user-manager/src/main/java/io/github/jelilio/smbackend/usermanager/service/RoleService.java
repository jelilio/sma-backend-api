package io.github.jelilio.smbackend.usermanager.service;

import io.github.jelilio.smbackend.usermanager.dto.RoleDto;
import io.github.jelilio.smbackend.usermanager.model.Role;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import java.util.Set;

public interface RoleService {
  Multi<RoleDto> findAll();
  Uni<RoleDto> findOne(String name);
  Uni<Boolean> checkIfExist(String name);
  Uni<RoleDto> create(RoleDto role);
  Uni<RoleDto> update(String name, RoleDto role);
  Uni<Void> delete(String name);
  Multi<Role> convertToActualRoles(Set<String> roles);
}
