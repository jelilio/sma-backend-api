package io.github.jelilio.smbackend.common.dto;

import io.github.jelilio.smbackend.common.dto.response.SchoolRes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseReq(
    @Size(min = 5, max = 100)
    @NotNull @NotBlank
    String name,
    @Size(max = 500)
    String description,
    @Valid @NotNull
    SchoolRes school
) {
}
