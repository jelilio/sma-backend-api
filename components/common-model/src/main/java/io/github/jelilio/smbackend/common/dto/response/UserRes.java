package io.github.jelilio.smbackend.common.dto.response;

import io.github.jelilio.smbackend.common.entity.enumeration.UserType;

import java.time.Instant;
import java.time.LocalDate;

public record UserRes(
    String id,
    String oidcId,
    String name,
    String email,
    String username,
    boolean enabled,
    String idNumber,
    LocalDate birthDate,
    Instant claimedDate,
    UserType type,
    Integer level,
    LocalDate startYear,
    LocalDate endYear,
    CourseRes course,
    SchoolRes school,
    String avatarUrl,
    boolean claimed,
    boolean verified,
    boolean requestVerification,
    Instant requestDate
) {
  boolean isClaimed() {
    return claimedDate != null;
  }
}
