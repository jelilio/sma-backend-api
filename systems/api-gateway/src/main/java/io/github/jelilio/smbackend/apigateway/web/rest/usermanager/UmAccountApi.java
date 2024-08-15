package io.github.jelilio.smbackend.apigateway.web.rest.usermanager;

import io.github.jelilio.smbackend.apigateway.client.usermanager.UmAccountProxy;
import io.github.jelilio.smbackend.apigateway.client.usermanager.UmUsersProxy;
import io.github.jelilio.smbackend.common.dto.AccountCheck;
import io.github.jelilio.smbackend.common.dto.RegisterDto;
import io.github.jelilio.smbackend.common.dto.ResendOtpDto;
import io.github.jelilio.smbackend.common.dto.ValidateOtpDto;
import io.github.jelilio.smbackend.common.dto.response.RegisterRes;
import io.github.jelilio.smbackend.common.entity.enumeration.AccountStatus;
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
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/usermanager/api")
public class UmAccountApi {
  private static final Logger logger = LoggerFactory.getLogger(UmAccountApi.class);

  @Inject
  @RestClient
  UmAccountProxy userManagerApi;

  @Inject
  @RestClient
  UmUsersProxy usersProxy;

  @POST
  @Path("/account/verify-email-otp")
  @Operation(summary = "Verify a user's email address using OTP")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = String.class)))
  public Uni<Response> verifyEmail(@Valid @RequestBody ValidateOtpDto dto) {
    return userManagerApi.verifyEmail(dto);
  }


  @POST
  @Path("/account/resend-email-otp")
  @Operation(summary = "Verify a user's email address using OTP")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = String.class)))
  public Uni<Response> verifyEmail(@Valid @RequestBody ResendOtpDto dto) {
    return userManagerApi.resendEmailOtp(dto);
  }


  @POST
  @Path("/account/register")
  public Uni<RegisterRes> register(@Valid @RequestBody RegisterDto dto) {
    return userManagerApi.register(dto);
  }

  @POST
  @Path("/account/check")
  public Uni<AccountStatus> checkAccount(@Valid @RequestBody AccountCheck dto) {
    return userManagerApi.checkAccount(dto);
  }

  @GET
  @Path("/users/{id}")
  public Uni<Response> avatar(@PathParam("id") String id) {
    logger.info("get avatar: {}", id);
    return Uni.createFrom().item(Response.ok().entity(Map.of("id", id)).build());
  }
}
