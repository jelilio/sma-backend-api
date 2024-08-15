package io.github.jelilio.smbackend.newsfeed.service;

import io.github.jelilio.smbackend.common.dto.PostDto;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.newsfeed.entity.Post;
import io.github.jelilio.smbackend.newsfeed.entity.projection.PostPro;
import io.github.jelilio.smbackend.newsfeed.entity.projection.PostReal;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.UUID;

public interface PostService {
  Uni<Post> findById(String id);

  Uni<Post> findByIdOrNull(UUID id);

  Uni<PostPro> findPostById(String userId, String id);

  Uni<PostPro> findPostById(String loggedIdUserId, String ownerId, String postId);

  // Authorised user can view all posts
  Uni<Paged<PostReal>> findAll(int size, int index);

  Uni<Paged<PostPro>> findAll(String userId, int size, int index);

  // logged-in user's post and following's post
  Uni<List<Post>> findAll(String userId);

  Uni<Paged<PostPro>> searchAll(String userId, String text, int size, int index);

  // logged-in user's post and following's post
  Uni<Paged<PostPro>> findFollowed(String userId, int size, int index);

  // logged-in user's post and following's post
  Uni<Paged<PostPro>> findFollowedCommunity(String userId, int size, int index);

  // logged-in user's post
  Uni<Paged<PostPro>> findMyPosts(String userId, int size, int index);

  // find community/club's posts by member
  Uni<Paged<PostPro>> findOtherUserPosts(String userId, String communityId, int size, int index);

  @Deprecated
  Uni<Paged<Post>> findUserPosts(String userId, int size, int index);

  Uni<Post> share(String userId, String postId);

  Uni<Post> like(String userId, String postId);

  Uni<Post> unshared(String userId, String postId);

  Uni<Post> unliked(String userId, String postId);

  Uni<Post> create(String userId, PostDto postDto);

  Uni<Post> reply(String userId, String parentPostId, PostDto postDto);

  Uni<Long> delete(String userId, String postId);

  Uni<Long> deleteByPost(String userId, String postId);

  Uni<Paged<PostPro>> findPostReplies(String userId, String postId, int size, int index);
}
