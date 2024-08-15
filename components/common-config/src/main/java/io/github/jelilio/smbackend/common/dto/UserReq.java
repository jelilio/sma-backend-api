package io.github.jelilio.smbackend.common.dto;

import io.github.jelilio.smbackend.common.dto.response.CourseRes;
import io.github.jelilio.smbackend.common.dto.response.SchoolRes;
import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UserReq (
    @NotNull @NotBlank @NotEmpty
    String idNumber,
    @NotNull @NotBlank @NotEmpty
    String name,
    @NotNull @NotBlank @NotEmpty
    String email,
    LocalDate birthDate,
    UserType type,
    CourseRes course,
    SchoolRes school,
    LocalDate startYear,
    LocalDate endYear,
    Integer level
){
}
