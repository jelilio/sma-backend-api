package io.github.jelilio.smbackend.newsfeed.web.rest.user;

import io.github.jelilio.smbackend.common.dto.RegisterCommunityDto;
import io.github.jelilio.smbackend.newsfeed.entity.User;
import io.github.jelilio.smbackend.newsfeed.service.CommunityService;
import io.github.jelilio.smbackend.newsfeed.service.UserService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.jelilio.smbackend.common.utils.Constants.USER_ID;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@WithSession
@RequestScoped
@Path("/api/user/staff")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed("newsfeed.ROLE_USER_STF")
public class StaffResource {
  private static final Logger logger = LoggerFactory.getLogger(StaffResource.class);
  @Claim(USER_ID)
  String loggedInUserId;

  @Inject
  UserService userService;

  @Inject
  CommunityService communityService;

  @POST
  @Path("/community") // create community
  public Uni<User> register(@Valid @RequestBody RegisterCommunityDto dto) {
    return communityService.createCommunity(loggedInUserId, dto);
  }
}
