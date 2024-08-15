package io.github.jelilio.smbackend.newsfeed.web.rest.user;


import io.github.jelilio.smbackend.common.dto.PostDto;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.newsfeed.entity.Post;
import io.github.jelilio.smbackend.newsfeed.entity.projection.PostPro;
import io.github.jelilio.smbackend.newsfeed.service.PostService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static io.github.jelilio.smbackend.common.utils.Constants.USER_ID;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@WithSession
@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed("newsfeed.ROLE_USER")
@Path("/api/user/posts")
public class PostResource {
  private static final Logger logger = LoggerFactory.getLogger(PostResource.class);

  @Claim(USER_ID)
  String userId;

  @Inject
  PostService postService;

  @POST
  @Path("/{id}/share")
  @Operation(summary = "Get a post")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Post.class)))
  public Uni<Response> share(@PathParam("id") String id) {
    return postService.share(userId, id)
        .onItem().transform(item -> Response.ok(item).build());
  }

  @POST
  @Path("/{id}/unshared")
  @Operation(summary = "Get a post")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Post.class)))
  public Uni<Response> unshared(@PathParam("id") String id) {
    return postService.unshared(userId, id)
        .onItem().transform(item -> Response.ok(item).build());
  }

  @POST
  @Path("/{id}/like")
  @Operation(summary = "Get a post")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Post.class)))
  public Uni<Response> like(@PathParam("id") String id) {
    return postService.like(userId, id)
        .onItem().transform(item -> Response.ok(item).build());
  }

  @POST
  @Path("/{id}/unliked")
  @Operation(summary = "Get a post")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Post.class)))
  public Uni<Response> unliked(@PathParam("id") String id) {
    return postService.unliked(userId, id)
        .onItem().transform(item -> Response.ok(item).build());
  }

  @GET
  @Path("/all")
  @Operation(summary = "Authenticated user fetch a paginated list of his posts")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Post.class)))
  public Uni<Paged<PostPro>> getAll(@BeanParam PageRequest pageRequest) {
    logger.info("getAll: size: {}, page: {}", pageRequest.size, pageRequest.page);
    return postService.findAll(userId, pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("/search")
  @Operation(summary = "Authenticated user fetch a paginated list of his posts")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Post.class)))
  public Uni<Paged<PostPro>> searchAll(@QueryParam("q") String text, @BeanParam PageRequest pageRequest) {
    logger.info("searchAll: q: {}, size: {}, page: {}", text, pageRequest.size, pageRequest.page);
    return postService.searchAll(userId, text, pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("/followed")
  @Operation(summary = "Authenticated user fetch a paginated list of his posts")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Post.class)))
  public Uni<Paged<PostPro>> followed(@BeanParam PageRequest pageRequest) {
    return postService.findFollowed(userId, pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("/communities")
  @Operation(summary = "Authenticated user fetch a paginated list of his posts")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Post.class)))
  public Uni<Paged<PostPro>> followedCommunities(@BeanParam PageRequest pageRequest) {
    return postService.findFollowed(userId, pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("/my")
  @Operation(summary = "Authenticated user fetch a paginated list of his posts")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Post.class)))
  public Uni<Paged<PostPro>> get(@BeanParam PageRequest pageRequest) {
    return postService.findMyPosts(userId, pageRequest.size, pageRequest.page);
  }

  @POST
  @Path("")
  @Consumes(MULTIPART_FORM_DATA)
  @Operation(summary = "User create a post")
  @APIResponse(responseCode = "201", description = "User's post successfully created",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Post.class)))
  public Uni<Response> create(@Valid @BeanParam PostDto dto) {
    logger.debug("creating: post: {}", dto.caption());

    return postService.create(userId, dto)
        .map(inserted -> Response
            .created(URI.create("/api/user/posts" + inserted.id))
            .entity(inserted)
            .build());
  }

  @POST
  @Path("/{id}/replies")
  @Consumes(MULTIPART_FORM_DATA)
  @Operation(summary = "User reply a post")
  @APIResponse(responseCode = "201", description = "User's post successfully created",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Post.class)))
  public Uni<Response> reply(@PathParam("id") String id, @Valid @BeanParam PostDto dto) {
    logger.debug("replying a post, id: {}, caption: {}", id, dto.caption());

    return postService.reply(userId, id, dto)
        .map(inserted -> Response
            .created(URI.create(String.format("/api/user/posts/%s/replies/%s", id, inserted.id)))
            .entity(inserted)
            .build());
  }

  @GET
  @Path("/{id}/replies")
  @Operation(summary = "Authenticated user fetch a paginated list of post replies")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Post.class)))
  public Uni<Paged<PostPro>> replies(@PathParam("id") String postId, @BeanParam PageRequest pageRequest) {
    return postService.findPostReplies(userId, postId, pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "Authenticated user fetch a paginated list of his posts")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Post.class)))
  public Uni<Response> get(@PathParam("id") String id) {
    return postService.findPostById(userId, id).onItem().transform(item -> Response.ok().entity(item).build());
  }

  @DELETE
  @Path("/{id}")
  @Operation(summary = "Delete a post")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema()))
  public Uni<Response> delete(@PathParam("id") String id) {
    return postService.delete(userId, id)
        .onItem().transform(item -> Response.ok().build());
  }
}
