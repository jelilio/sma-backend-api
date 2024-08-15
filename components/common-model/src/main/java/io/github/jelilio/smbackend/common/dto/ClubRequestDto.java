package io.github.jelilio.smbackend.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClubRequestDto(
    @NotNull @NotBlank
    @Size(min = 5, max = 100)
    String name,
    @NotNull @NotBlank @Size(min = 10, max = 500)
    String purpose,
//    @NotNull @NotBlank
    String communityId
) {}
