package io.github.jelilio.smbackend.newsfeed.entity.projection;

import io.quarkus.hibernate.reactive.panache.common.ProjectedFieldName;

public class FollowOnlyCount {
  public long followers;
  public long followings;

  public FollowOnlyCount(
      @ProjectedFieldName("followings") long followings,
      @ProjectedFieldName("followers") long followers
  ) {
    this.followings = followings;
    this.followers = followers;
  }
}
