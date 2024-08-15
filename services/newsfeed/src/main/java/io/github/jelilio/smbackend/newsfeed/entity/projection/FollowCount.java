package io.github.jelilio.smbackend.newsfeed.entity.projection;

import io.quarkus.hibernate.reactive.panache.common.ProjectedFieldName;

public class FollowCount {
  public long followers;
  public long followings;
  public long communities;
  public long clubs;

  public FollowCount(
      @ProjectedFieldName("followings") long followings,
      @ProjectedFieldName("communities") long communities,
      @ProjectedFieldName("clubs") long clubs,
      @ProjectedFieldName("followers") long followers
  ) {
    this.followings = followings;
    this.followers = followers;
    this.communities = communities;
    this.clubs = clubs;
  }
}
