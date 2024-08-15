package io.github.jelilio.smbackend.usermanager.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public class Role {
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String id;
  public String name;
  public String description;
  public boolean composite;
  //@JsonInclude(JsonInclude.Include.NON_NULL)
  //public Set<Role> compositeRoles = new HashSet<>();

  public Role() {}

  public Role(String name, String description, Boolean composite) {
    this.name = name;
    this.description = description;
    this.composite = composite;
    //this.compositeRoles = null;
  }

  public Role(String id, String name, String description, Boolean composite) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.composite = composite;
    //this.compositeRoles = null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Role role = (Role) o;
    return Objects.equals(name, role.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return "Role{" +
        "name='" + name + '\'' +
        '}';
  }
}
