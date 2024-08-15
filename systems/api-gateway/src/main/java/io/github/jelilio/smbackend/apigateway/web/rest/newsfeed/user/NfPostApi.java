package io.github.jelilio.smbackend.apigateway.web.rest.newsfeed.user;


import io.github.jelilio.smbackend.apigateway.client.newsfeed.user.NfPostProxy;
import io.github.jelilio.smbackend.common.dto.PostDto;
import io.github.jelilio.smbackend.common.dto.response.PostRes;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.commonutil.dto.response.PostProRes;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;


@RequestScoped
@Authenticated
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/newsfeed/api/user/posts")
public class NfPostApi {
  private static final Logger logger = LoggerFactory.getLogger(NfPostApi.class);

  @Inject
  @RestClient
  NfPostProxy nfPostProxy;

  @POST
  @Path("/{id}/share")
  @Operation(summary = "Share a post")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = PostRes.class)))
  public Uni<Response> share(@PathParam("id") String id) {
    return nfPostProxy.share(id);
  }

  @POST
  @Path("/{id}/unshared")
  @Operation(summary = "Unshared a post")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = PostRes.class)))
  public Uni<Response> unshared(@PathParam("id") String id) {
    return nfPostProxy.unshared(id);
  }

  @POST
  @Path("/{id}/like")
  @Operation(summary = "Like a post")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = PostRes.class)))
  public Uni<Response> like(@PathParam("id") String id) {
    return nfPostProxy.like(id);
  }

  @POST
  @Path("/{id}/unliked")
  @Operation(summary = "Unliked a post")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = PostRes.class)))
  public Uni<Response> unliked(@PathParam("id") String id) {
    return nfPostProxy.unliked(id);
  }

  @GET
  @Path("/my")
  @Authenticated
  @Operation(summary = "Authenticated user fetch a paginated list of his posts")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = PostRes.class)))
  public Uni<Paged<PostProRes>> get(@BeanParam PageRequest pageRequest) {
    return nfPostProxy.get(pageRequest);
  }

  @GET
  @Path("/all")
  @Authenticated
  @Operation(summary = "Authenticated user fetch a paginated list of his posts")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = PostRes.class)))
  public Uni<Paged<PostProRes>> all(@BeanParam PageRequest pageRequest) {
    return nfPostProxy.all(pageRequest);
  }

  @GET
  @Path("/search")
  @Authenticated
  @Operation(summary = "Authenticated user fetch a paginated list of his posts")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = PostRes.class)))
  public Uni<Paged<PostProRes>> search(@QueryParam("q") String text, @BeanParam PageRequest pageRequest) {
    return nfPostProxy.search(text, pageRequest);
  }

  @GET
  @Path("/followed")
  @Authenticated
  @Operation(summary = "Authenticated user fetch a paginated list of his posts")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = PostRes.class)))
  public Uni<Paged<PostProRes>> followed(@BeanParam PageRequest pageRequest) {
    return nfPostProxy.followed(pageRequest);
  }

  @GET
  @Path("/communities")
  @Authenticated
  @Operation(summary = "Authenticated user fetch a paginated list of his posts")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = PostRes.class)))
  public Uni<Paged<PostProRes>> communities(@BeanParam PageRequest pageRequest) {
    return nfPostProxy.followedCommunities(pageRequest);
  }

  @POST
  @Path("")
  @Consumes(MULTIPART_FORM_DATA)
  @Operation(summary = "User create a post")
  @APIResponse(responseCode = "201", description = "User's post successfully created",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = PostRes.class)))
  public Uni<Response> create(@Valid @BeanParam PostDto dto) {
    logger.debug("creating: post: {}", dto.caption());

    return nfPostProxy.create(dto);
  }

  @POST
  @Path("/{id}/replies")
  @Consumes(MULTIPART_FORM_DATA)
  @Operation(summary = "User reply a post")
  @APIResponse(responseCode = "201", description = "User's post successfully created",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = PostRes.class)))
  public Uni<Response> reply(@PathParam("id") String id, @Valid @BeanParam PostDto dto) {
    logger.debug("replying: post: {}", dto.caption());

    return nfPostProxy.reply(id, dto);
  }

  @GET
  @Path("/{id}/replies")
  @Operation(summary = "Authenticated user fetch a paginated list of his posts")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = PostRes.class)))
  public Uni<Paged<PostProRes>> replies(@PathParam("id") String id, @BeanParam PageRequest pageRequest) {
    return nfPostProxy.replies(id, pageRequest);
  }


  @GET
  @Path("/{id}")
  @Operation(summary = "Fetch a post")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = PostRes.class)))
  public Uni<Response> get(@PathParam("id") String id) {
    return nfPostProxy.get(id);
  }

  @DELETE
  @Path("/{id}")
  @Operation(summary = "Delete a post")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema()))
  public Uni<Response> delete(@PathParam("id") String id) {
    return nfPostProxy.delete(id);
  }
}
