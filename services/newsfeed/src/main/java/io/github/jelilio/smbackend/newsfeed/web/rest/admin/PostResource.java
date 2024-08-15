package io.github.jelilio.smbackend.newsfeed.web.rest.admin;

import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.newsfeed.entity.Post;
import io.github.jelilio.smbackend.newsfeed.entity.projection.PostReal;
import io.github.jelilio.smbackend.newsfeed.service.PostService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@WithSession
@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed("newsfeed.ROLE_VIEW_POSTS")
@Path("/api/admin/posts")
public class PostResource {
  private static final Logger logger = LoggerFactory.getLogger(PostResource.class);
  @Inject
  PostService postService;

  @GET
  @Path("")
  @Operation(summary = "Authorised user fetch a paginated list of his posts")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Post.class)))
  public Uni<Paged<PostReal>> get(@BeanParam PageRequest pageRequest) {
    return postService.findAll(pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "Authorised user fetch a posts")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Post.class)))
  public Uni<Response> get(@PathParam("id") String id) {
    return postService.findById(id).onItem().transform(item -> Response.ok().entity(item).build());
  }
}
