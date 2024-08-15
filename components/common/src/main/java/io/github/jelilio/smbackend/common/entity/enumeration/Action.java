package io.github.jelilio.smbackend.common.entity.enumeration;

public enum Action {
  NONE("No Action", "No action to be taken", null),
  WARN_USER("Warn user", "Warn the user", null),
  SUSPEND_USER("Suspend user", "Suspend the user", WARN_USER),
  DELETE_POST("Delete post", "Mark the post as delete", null);

  final String title;
  final String description;
  final Action dependsOn;

  Action(String title, String description, Action dependsOn) {
    this.title = title;
    this.description = description;
    this.dependsOn = dependsOn;
  }
}
