package io.github.jelilio.smbackend.newsfeed.web.rest.user;

import io.github.jelilio.smbackend.newsfeed.service.UserService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.jelilio.smbackend.common.utils.Constants.USER_ID;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@WithSession
@RequestScoped
@Path("/api/user/member22")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed("newsfeed.ROLE_USER")
public class MemberResource {
  private static final Logger logger = LoggerFactory.getLogger(MemberResource.class);
  @Claim(USER_ID)
  String userId;

  @Inject
  UserService userService;

//  @GET
//  @Path("/communities/all")
//  public Uni<List<Community>> communities() {
//    logger.debug("fetch communities of: member: {}", userId);
//    return userService.communities_(userId);
//  }
//
//  @GET
//  @Path("/communities")
//  public Uni<Paged<Community>> communities(@BeanParam PageRequest pageRequest) {
//    logger.debug("fetch communities of: member: {}", userId);
//    return userService.communities(userId, pageRequest.size, pageRequest.page);
//  }

  // currently using follow() in UserResource
  @POST
  @Path("/request") // user request to be a member of a community
  public Uni<Response> requestForMembership(@RequestBody String userCommId) {
    logger.debug("follow: userId: {}, otherUserId: {}", userId, userCommId);
    return userService.requestForMembership(userId, userCommId)
        .onItem().transform(item -> Response.ok()
            .entity(item).build());
  }

  // to be deprecated
  /*
  @POST
  @Path("/club/request")
  public Uni<Response> requestForClubCreation(@RequestBody @Valid ClubRequestDto dto) {
    logger.debug("member request for club creation: userId: {}, otherUserId: {}", userId, dto);
    return userService.clubRequest(userId, dto).onItem()
        .transform(it -> Response.created(URI.create("/")).entity(it).build());
  }*/
}
