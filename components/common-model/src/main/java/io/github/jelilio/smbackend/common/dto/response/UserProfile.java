package io.github.jelilio.smbackend.common.dto.response;


import io.github.jelilio.smbackend.common.entity.enumeration.UserType;

import java.time.Instant;
import java.time.LocalDate;

public class UserProfile {
  public String id;
  public UserType userType;
  public String name;
  public String username;
  public String bio;
  public String avatarUrl;
  public String avatarType;
  public Boolean enabled;
  public String idNumber;
  public FollowStatusCount followCount;
  public FollowStatus followStatus;
  public MemberStatusCount memberCount;
  public MemberStatus memberStatus;
  public ClubStatusCount clubCount;
  public MemberStatus clubStatus;
  public OwnerRes owner;

  public Instant createdDate;
  public Instant verifiedDate;
  public LocalDate birthDate;

  public UserProfile() {}

  private UserProfile(UserProfileBuilder builder) {
    this.id = builder.id;
    this.name = builder.name;
    this.username = builder.username;
    this.userType = builder.userType;
    this.bio = builder.bio;
    this.avatarType = builder.avatarType;
    this.avatarUrl = builder.avatarUrl;
    this.enabled = builder.enabled;
    this.createdDate = builder.createdDate;
    this.verifiedDate = builder.verifiedDate;
    this.birthDate = builder.birthDate;
    this.idNumber = builder.idNumber;
    this.followCount = builder.followCount;
    this.followStatus = builder.followStatus;
    this.memberCount = builder.memberCount;
    this.memberStatus = builder.memberStatus;
    this.clubCount = builder.clubCount;
    this.clubStatus = builder.clubStatus;
    this.owner = builder.owner;
  }

  public boolean isVerified() {
    return verifiedDate != null;
  }

  public static class UserProfileBuilder {
    // required parameters
    private final String id;
    private final String name;
    private final String username;
    private final Boolean enabled;
    private final UserType userType;
    private final Instant createdDate;
    private final String bio;
    private final String avatarUrl;
    private final String avatarType;
    private final Instant verifiedDate;
    private final LocalDate birthDate;

    // optional parameters
    private String idNumber;
    private FollowStatusCount followCount;
    private FollowStatus followStatus;
    private MemberStatusCount memberCount;
    private MemberStatus memberStatus;
    private ClubStatusCount clubCount;
    private MemberStatus clubStatus;
    private OwnerRes owner;

    // constructor to set the required parameters
    public UserProfileBuilder(
        String id, UserType userType, String name, String username, String bio, String avatarUrl, String avatarType,
        boolean enabled, Instant createdDate, Instant verifiedDate, LocalDate birthDate) {
      this.id = id;
      this.name = name;
      this.username = username;
      this.enabled = enabled;
      this.userType = userType;
      this.createdDate = createdDate;
      this.bio = bio;
      this.avatarUrl = avatarUrl;
      this.avatarType = avatarType;
      this.verifiedDate = verifiedDate;
      this.birthDate = birthDate;
    }

    public UserProfileBuilder setIdNumber(String idNumber) {
      this.idNumber = idNumber;
      return this;
    }

    public UserProfileBuilder setFollowCount(FollowStatusCount followCount) {
      this.followCount = followCount;
      return this;
    }

    public UserProfileBuilder setFollowStatus(FollowStatus followStatus) {
      this.followStatus = followStatus;
      return this;
    }

    public UserProfileBuilder setMemberCount(MemberStatusCount memberCount) {
      this.memberCount = memberCount;
      return this;
    }

    public UserProfileBuilder setMemberStatus(MemberStatus memberStatus) {
      this.memberStatus = memberStatus;
      return this;
    }

    public UserProfileBuilder setClubCount(ClubStatusCount clubCount) {
      this.clubCount = clubCount;
      return this;
    }

    public UserProfileBuilder setClubStatus(MemberStatus clubStatus) {
      this.clubStatus = clubStatus;
      return this;
    }

    public UserProfileBuilder setOwner(OwnerRes owner) {
      this.owner = owner;
      return this;
    }

    public UserProfile build() {
      return new UserProfile(this);
    }
  }
}
