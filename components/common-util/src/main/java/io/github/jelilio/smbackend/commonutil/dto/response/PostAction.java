package io.github.jelilio.smbackend.commonutil.dto.response;

public record PostAction(
    Boolean liked,
    Boolean shared,
    Boolean replied
) {
}
