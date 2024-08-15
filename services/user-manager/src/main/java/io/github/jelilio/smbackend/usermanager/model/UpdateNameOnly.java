package io.github.jelilio.smbackend.usermanager.model;

import jakarta.validation.constraints.NotNull;

import static io.github.jelilio.smbackend.usermanager.utils.StringUtils.splitName;

public record UpdateNameOnly(
    @NotNull
    String firstName,
    String lastName
) {
    public UpdateNameOnly(String name) {
        this(splitName(name).first(), splitName(name).second());
    }
}
