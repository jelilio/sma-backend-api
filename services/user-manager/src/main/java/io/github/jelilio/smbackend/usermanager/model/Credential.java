package io.github.jelilio.smbackend.usermanager.model;

public record Credential(String value, boolean temporary, String type) {
  public Credential(String value) {
    this(value, true, "password");
  }

  public Credential(String value, boolean temporary) {
    this(value, temporary, "password");
  }

  public Credential(String value, boolean temporary, String type) {
    this.value = value;
    this.temporary = temporary;
    this.type = type;
  }
}
