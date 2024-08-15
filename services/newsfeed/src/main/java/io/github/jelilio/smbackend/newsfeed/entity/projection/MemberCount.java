package io.github.jelilio.smbackend.newsfeed.entity.projection;

import io.quarkus.hibernate.reactive.panache.common.ProjectedFieldName;

public class MemberCount {
//  private static final Logger logger = LoggerFactory.getLogger(UserResource.class);

  public long members;
  public long pendings;

  public MemberCount(@ProjectedFieldName("members") long members, @ProjectedFieldName("pendings")  long pendings) {
    this.members = members;
    this.pendings = pendings;
  }
}
