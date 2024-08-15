package io.github.jelilio.smbackend.apigateway.web.rest.newsfeed.user;

import io.github.jelilio.smbackend.apigateway.client.newsfeed.user.NfUserProxy;
import io.github.jelilio.smbackend.common.dto.response.*;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.commonutil.dto.response.NotificationRes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/newsfeed/api/user")
public class NfUserApi {
  private static final Logger logger = LoggerFactory.getLogger(NfUserApi.class);

  @Inject
  @RestClient
  NfUserProxy nfUserProxy;

  @GET
  @Path("/profile")
  public Uni<UserProfile> profile() {
    return nfUserProxy.profile();
  }

  @GET
  @Path("/search")
  public Uni<Paged<UserCommunityRes>> searchAll(@QueryParam("q") String query, @BeanParam PageRequest pageRequest) {
    logger.debug("searchAll: query: {}, pageRequest: {}", query, pageRequest);
    return nfUserProxy.searchAll(query, pageRequest);
  }

  @GET
  @Path("/followings")
  public Uni<Paged<FollowRes>> followings(@BeanParam PageRequest pageRequest) {
    logger.debug("followings: pageRequest: {}", pageRequest);
    return nfUserProxy.followings(pageRequest);
  }

  @GET
  @Path("/followers")
  public Uni<Paged<FollowRes>> followers(@BeanParam PageRequest pageRequest) {
    logger.debug("followers: pageRequest: {}", pageRequest);
    return nfUserProxy.followers(pageRequest);
  }

  @GET
  @Path("/pendings")
  public Uni<Paged<MemberRes>> pendings(@BeanParam PageRequest pageRequest) {
    logger.info("pendings: pageRequest: {}", pageRequest);
    return nfUserProxy.pendings(pageRequest);
  }

  @GET
  @Path("/members")
  public Uni<Paged<MemberRes>> members(@BeanParam PageRequest pageRequest) {
    logger.info("members: pageRequest: {}", pageRequest);
    return nfUserProxy.members(pageRequest);
  }

  @GET
  @Path("/notifications")
  public Uni<Paged<NotificationRes>> notifications(@BeanParam PageRequest pageRequest) {
    logger.info("notifications: pageRequest: {}", pageRequest);
    return nfUserProxy.notifications(pageRequest);
  }

  @GET
  @Path("/communities")
  public Uni<Paged<FollowComClubRes>> communities(@BeanParam PageRequest pageRequest) {
    logger.debug("communities: pageRequest: {}", pageRequest);
    return nfUserProxy.communities(pageRequest);
  }

  @GET
  @Path("/clubs")
  public Uni<Paged<FollowComClubRes>> clubs(@BeanParam PageRequest pageRequest) {
    logger.debug("clubs: pageRequest: {}", pageRequest);
    return nfUserProxy.clubs(pageRequest);
  }
}
