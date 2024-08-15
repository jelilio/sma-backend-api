package io.github.jelilio.smbackend.common.dto.response;

import jakarta.validation.constraints.NotNull;

public record EnableUserReq(
    @NotNull
    Boolean enabled
) {
}
