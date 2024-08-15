package io.github.jelilio.smbackend.usermanager.model;

import jakarta.validation.constraints.NotNull;

import static io.github.jelilio.smbackend.usermanager.utils.StringUtils.splitName;

public record UpdateName(
    @NotNull
    String firstName,
    String lastName,
    String username
) {
    public UpdateName(String name, String username) {
        this(splitName(name).first(), splitName(name).second(), username);
    }
}
