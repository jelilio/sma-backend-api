package io.github.jelilio.smbackend.usermanager.model;

import jakarta.validation.constraints.NotNull;

public record EnableUser (
    @NotNull
    Boolean enabled
) {
}