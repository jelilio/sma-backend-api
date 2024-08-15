package io.github.jelilio.smbackend.usermanager.model;

import jakarta.validation.constraints.NotNull;

public record EnableVerifyEmail(
    @NotNull
    Boolean enabled,
    @NotNull
    Boolean emailVerified
) {
}
