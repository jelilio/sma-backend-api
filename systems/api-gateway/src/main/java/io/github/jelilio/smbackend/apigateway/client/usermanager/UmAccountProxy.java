package io.github.jelilio.smbackend.apigateway.client.usermanager;

import io.github.jelilio.smbackend.common.dto.*;
import io.github.jelilio.smbackend.common.dto.response.RegisterRes;
import io.github.jelilio.smbackend.common.entity.enumeration.AccountStatus;
import io.github.jelilio.smbackend.common.exception.mapper.ClientExceptionMapper;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "usermanager-api")
//@RegisterProvider(AccessTokenRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(ClientExceptionMapper.class)})
public interface UmAccountProxy {
  @POST
  @Path("/account/register")
  Uni<RegisterRes> register(@Valid @RequestBody RegisterDto dto);

  @POST
  @Path("/account/check")
  Uni<AccountStatus> checkAccount(@Valid @RequestBody AccountCheck dto);

  @POST
  @Path("/account/verify-email-otp")
  Uni<Response> verifyEmail(@Valid @RequestBody ValidateOtpDto dto);

  @POST
  @Path("/account/resend-email-otp")
  Uni<Response> resendEmailOtp(@Valid @RequestBody ResendOtpDto dto);
}
