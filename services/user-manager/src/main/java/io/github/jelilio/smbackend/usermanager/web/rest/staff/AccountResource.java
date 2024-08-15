package io.github.jelilio.smbackend.usermanager.web.rest.staff;

import io.github.jelilio.smbackend.common.dto.RegisterCommunityDto;
import io.github.jelilio.smbackend.common.dto.response.RegisterRes;
import io.github.jelilio.smbackend.usermanager.service.UserService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.security.identity.SecurityIdentity;
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

import java.util.Set;

import static io.github.jelilio.smbackend.common.utils.Constants.USER_ID;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@WithSession
@RequestScoped
@Path("/api")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class AccountResource {
  private static final Logger logger = LoggerFactory.getLogger(AccountResource.class);
  @Claim(USER_ID)
  String loggedInUserId;

  @Inject
  UserService userService;

  @Inject
  SecurityIdentity securityIdentity;

  @POST
  @RolesAllowed("newsfeed.ROLE_USER_STF")
  @Path("/staff/account/register-community")
  public Uni<RegisterRes> register(@Valid @RequestBody RegisterCommunityDto dto) {
    logger.debug("register: {}", dto);
    return userService.registerCommunity(loggedInUserId, dto, Set.of("USER"));
  }
}
