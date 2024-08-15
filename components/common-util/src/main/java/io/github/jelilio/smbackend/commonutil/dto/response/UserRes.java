package io.github.jelilio.smbackend.commonutil.dto.response;

import io.github.jelilio.smbackend.commonutil.entity.enumeration.UserType;

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
    Integer age,
    Instant claimedDate,
    UserType type,
    Integer level,
    LocalDate startYear,
    LocalDate endYear,
    String avatarUrl,
    boolean claimed,
    boolean verified,
    boolean requestVerification,
    Instant requestDate,
    Instant verifiedDate
) {
  boolean isClaimed() {
    return claimedDate != null;
  }
}
