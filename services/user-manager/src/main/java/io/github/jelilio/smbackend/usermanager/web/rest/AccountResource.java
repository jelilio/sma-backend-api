package io.github.jelilio.smbackend.usermanager.web.rest;

import io.github.jelilio.smbackend.common.dto.AccountCheck;
import io.github.jelilio.smbackend.common.dto.RegisterDto;
import io.github.jelilio.smbackend.common.dto.ResendOtpDto;
import io.github.jelilio.smbackend.common.dto.ValidateOtpDto;
import io.github.jelilio.smbackend.common.dto.response.RegisterRes;
import io.github.jelilio.smbackend.common.entity.enumeration.AccountStatus;
import io.github.jelilio.smbackend.usermanager.model.KUser;
import io.github.jelilio.smbackend.usermanager.service.UserService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@WithSession
@RequestScoped
@Path("/api/account")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class AccountResource {
  private static final Logger logger = LoggerFactory.getLogger(AccountResource.class);
  @Inject
  UserService userService;

  @Inject
  SecurityIdentity securityIdentity;

  @POST
  @Path("/register")
  public Uni<RegisterRes> register(@Valid @RequestBody RegisterDto dto) {
    return userService.register(dto, Set.of("USER"));
  }

  @POST
  @Path("/check")
  public Uni<AccountStatus> checkAccount(@Valid @RequestBody AccountCheck dto) {
    return userService.checkAccount(dto.emailOrUsername());
  }

  @POST
  @Path("/verify-email-otp")
  @Operation(summary = "Verify a user's email address using OTP")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = String.class)))
  public Uni<Response> verifyEmail(@Valid @RequestBody ValidateOtpDto dto) {
    logger.info("verifyEmail: {}", dto.email());

    return userService.verifyEmail(dto.email(), dto.otpKey()).onItem()
        .transform(response -> Response.ok(response).build());
  }

  @POST
  @Path("/resend-email-otp")
  @Operation(summary = "Verify a user's email address using OTP")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = String.class)))
  public Uni<Response> resendEmailOtp(@Valid @RequestBody ResendOtpDto dto) {
    return userService.resendOtp(dto.emailOrUsername()).onItem()
        .transform(response -> Response.ok(response).build());
  }

  @GET
  @Path("/register")
  public Uni<KUser> info() {
    DefaultJWTCallerPrincipal principal = (DefaultJWTCallerPrincipal) securityIdentity.getPrincipal();
    return userService.findOne(principal.getSubject());
  }
}
