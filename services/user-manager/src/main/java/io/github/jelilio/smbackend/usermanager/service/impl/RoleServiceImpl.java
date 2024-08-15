package io.github.jelilio.smbackend.usermanager.service.impl;

import com.google.common.collect.Sets;
import io.github.jelilio.smbackend.usermanager.client.keycloak.RoleProxy;
import io.github.jelilio.smbackend.usermanager.dto.RoleDto;
import io.github.jelilio.smbackend.common.exception.AlreadyExistException;
import io.github.jelilio.smbackend.common.exception.NotFoundException;
import io.github.jelilio.smbackend.usermanager.model.Role;
import io.github.jelilio.smbackend.usermanager.service.RoleService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

@ApplicationScoped
public class RoleServiceImpl implements RoleService {
  private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

  @Inject
  @RestClient
  RoleProxy roleProxy;

  /**
   * Get all Roles
   * @return the Roles
   */
  @Override
  public Multi<RoleDto> findAll() {
    return roleProxy.getRoles().onItem()
        .transformToMulti(roles -> Multi.createFrom()
            .iterable(roles))
        .map(item -> new RoleDto(item.name, item.description, item.composite));
  }

  /**
   * Find a particular Role by name
   * @param name of the Role to find
   * @return the role entity
   */
  @Override
  public Uni<RoleDto> findOne(String name) {
    Uni<Role> uniRole = roleProxy.getRole(name);
    return uniRole.flatMap(role -> {
      if(!role.composite) {
        return Uni.createFrom().item(new RoleDto(role.name, role.description, false));
      }

      return roleProxy.getCompositeRoles(role.name)
          .onItem().transformToMulti(roles -> Multi.createFrom().iterable(roles))
          .map(item -> new RoleDto(item.name, item.description, item.composite, null))
          .collect().asList().map(HashSet::new)
          .map(it -> new RoleDto(role.name, role.description, role.composite, it));
    });
  }

  /**
   * Check if a role exist by a given name
   * @param name of the Role to check
   * @return either if the role exist or not
   */
  @Override
  public Uni<Boolean> checkIfExist(String name) {
    return roleProxy.getRole(name).onFailure()
        .recoverWithUni(item -> {
          if(item instanceof NotFoundException) {
            return Uni.createFrom().nullItem();
          }
          return Uni.createFrom().failure(new ServerErrorException(Response.Status.SERVICE_UNAVAILABLE));
        }).map(Objects::nonNull);
  }

  /**
   * Check if a role exist by a name but not a given role
   * @param name of the Role to check
   * @param role the given role
   * @return either if the role exist or not
   */
  public Uni<Boolean> checkIfExist(String name, RoleDto role) {
    return roleProxy.getRole(name).onFailure()
        .recoverWithUni(item -> {
          if(item instanceof NotFoundException) {
            return Uni.createFrom().nullItem();
          }
          return Uni.createFrom().failure(new ServerErrorException(Response.Status.SERVICE_UNAVAILABLE));
        }).map(item -> {
          if(item == null || role == null) return false;

          return !role.name().equals(item.name);
        });
  }

  /**
   * Update a given Role
   * @param role the role info to be created
   * @return the created Role
   */
  @Override
  public Uni<RoleDto> create(RoleDto role) {
    return checkIfExist(role.name()).flatMap(item -> {
      if(item) {
        return Uni.createFrom().failure(new AlreadyExistException("Role already exist"));
      }

      return roleProxy.createRole(new Role(role.name(), role.description(), role.composite()))
          .flatMap(it -> updateCompositeRoles(role.name(), new HashSet<>(),
              role.compositeRoles() == null? emptySet() : role.compositeRoles()))
          .map(compositeRoles -> new RoleDto(role.name(), role.description(), role.composite(), compositeRoles));
    });
  }

  /**
   * Update a given Role
   * @param name of the Role
   * @param role the role info to be updated with
   * @return the updated Role
   */
  @Override
  public Uni<RoleDto> update(String name, RoleDto role) {
    return findOne(name).flatMap(ext ->
        checkIfExist(role.name(), ext).flatMap(item -> {
          if(item) {
            return Uni.createFrom().failure(new AlreadyExistException("Role already exist"));
          }

          return roleProxy.updateRole(name, new Role(role.name(), role.description(), role.composite())).flatMap(
              it -> updateCompositeRoles(name, ext.compositeRoles(), role.compositeRoles() == null? emptySet() : role.compositeRoles())
          ).map(compositeRoles ->new RoleDto(role.name(), role.description(), role.composite(), compositeRoles));
        }));
  }

  /**
   * Delete a given Role
   * @param name of the Role to be deleted
   * @return Void
   */
  @Override
  public Uni<Void> delete(String name) {
    return roleProxy.deleteRole(name);
  }

  /**
   * Update a given composite Role by adding or removing
   * a set of composite-roles
   * @param name of the Role
   * @param extCompositeRoles existing set of composite-roles
   * @param compositeRoles new set of composite-roles
   * @return set of updated composite-roles
   */
  private Uni<Set<RoleDto>> updateCompositeRoles(String name, Set<RoleDto> extCompositeRoles, Set<RoleDto> compositeRoles) {
    if(compositeRoles.isEmpty() && extCompositeRoles.isEmpty()) return Uni.createFrom().item(emptySet());

    Set<RoleDto> whatToAdd = Sets.difference(compositeRoles, extCompositeRoles);
    Set<RoleDto> whatToRemove = Sets.difference(extCompositeRoles, compositeRoles);

    return removeCompositeRoles(name, whatToRemove)
        .flatMap(removed -> {
          var bal = Sets.difference(extCompositeRoles, removed);
          var res =  addCompositeRoles(name, whatToAdd);
          return res.map(added -> Sets.union(added, bal));
        });
  }

  /**
   * Add a sets of composite-roles from a given Role
   * @param name name of the Role
   * @param compositeRoles set of composite-roles to be added
   * @return set of added roles
   */
  public Uni<Set<RoleDto>> addCompositeRoles(String name, Set<RoleDto> compositeRoles) {
    if(compositeRoles.isEmpty()) return Uni.createFrom().item(emptySet());

    Multi<Role> multiRoles = convertToActualRoles(compositeRoles.stream()
        .map(RoleDto::name).collect(Collectors.toSet()));

    Uni<Response> result = multiRoles.collect().asList()
        .flatMap(roles -> roleProxy.addCompositeRoles(name, new HashSet<>(roles)));

    return result.flatMap(item -> multiRoles
        .map(it -> new RoleDto(it.name, it.description, it.composite))
        .collect().asList().map(HashSet::new));
  }

  /**
   * Remove a sets of composite-roles from a given Role
   * @param name name of the Role
   * @param compositeRoles set of composite-roles to be removed
   * @return set of removed roles
   */
  public Uni<Set<RoleDto>> removeCompositeRoles(String name, Set<RoleDto> compositeRoles) {
    if(compositeRoles.isEmpty()) return Uni.createFrom().item(emptySet());

    Multi<Role> multiRoles = convertToActualRoles(compositeRoles.stream()
        .map(RoleDto::name).collect(Collectors.toSet()));

    Uni<Response> result = multiRoles.collect().asList()
        .flatMap(roles -> roleProxy.removeCompositeRoles(name, new HashSet<>(roles)));

    return result.flatMap(item -> multiRoles
        .map(it -> new RoleDto(it.name, it.description, it.composite))
        .collect().asList().map(HashSet::new));
  }

  /**
   * Convert roles to set of roles entity existing in keycloak
   * @param roles roles to be converted
   * @return roles entity
   */
  public Multi<Role> convertToActualRoles(Set<String> roles) {
    Uni<List<Role>> uniRoles = roleProxy.getRoles();

    Uni<List<Role>> roleObjects = uniRoles.map(r -> {
      List<Role> existingRoles = new ArrayList<>(r);

      List<String> serverRoles = existingRoles.stream().map(item -> item.name).toList();

      return roles.stream()
          .filter(serverRoles::contains)
          .map(it -> existingRoles.get(serverRoles.indexOf(it)))
          .collect(Collectors.toList());
    });

    return roleObjects.onItem().transformToMulti(items -> Multi.createFrom().iterable(items));
  }
}
