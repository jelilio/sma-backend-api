package io.github.jelilio.smbackend.newsfeed.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jelilio.smbackend.common.entity.base.AbstractAuditingEntity;
import io.github.jelilio.smbackend.common.entity.listener.AuditingEntityListener;
import io.github.jelilio.smbackend.newsfeed.entity.projection.Community;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Cacheable
@Table(name = "club_requests")
@EntityListeners(AuditingEntityListener.class)
public class ClubRequest extends AbstractAuditingEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Schema(readOnly = true)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public UUID id;

  @Column(name = "name", length = 100, nullable = false)
  public String name;

  @Column(name = "purpose", length = 500, nullable = false)
  public String purpose;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "requester_id")
  public User requester;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "community_id")
  public User community;

  @Column(name = "approval_date")
  public Instant approvalDate;

  @Column(name = "rejected")
  public boolean rejected;

  public static Uni<ClubRequest> findById(String id) {
    return findById(UUID.fromString(id));
  }

  public static Uni<ClubRequest> findByIdAndCommunity(String id, Community community) {
    return find("id = ? 1 and community = ?2",  UUID.fromString(id), community).firstResult();
  }

  public static Uni<Long> countByNameAndCommunity(String name, User community ) {
    return count("name = ?1 and community = ?2", name, community);
  }

  public static Uni<Long> countByIdAndCommunityAndPending(String id, User community ) {
    return count("id = ?1 and community = ?2 and approvalDate is null",  UUID.fromString(id), community);
  }

  public static Uni<ClubRequest> findByIdAndCommunityAndPending(String id, User community ) {
    return find("id = ?1 and community = ?2 and approvalDate is null",  UUID.fromString(id), community).firstResult();
  }

  public static Uni<Long> countByCommunityAndPending(String communityId ) {
    return count("community.id = ?1 and approvalDate is null",  UUID.fromString(communityId));
  }

  public static PanacheQuery<ClubRequest> findByCommunity(User community) {
    return ClubRequest.find("community = ?1", community);
  }

  public static PanacheQuery<ClubRequest> findPendingsByCommunity(User community) {
    return ClubRequest.find("community = ?1 and approvalDate is null", community);
  }

  public ClubRequest() {}

  public ClubRequest(String name, String purpose, User requester, User community) {
    this.name = name;
    this.purpose = purpose;
    this.requester = requester;
    this.community = community;
  }

  public boolean isApproved() {
    return approvalDate != null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ClubRequest clubRequest = (ClubRequest) o;
    return Objects.equals(id, clubRequest.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "ClubRequest{" +
        "id=" + id +
        ", name='" + name + '\'' +
        '}';
  }
}
