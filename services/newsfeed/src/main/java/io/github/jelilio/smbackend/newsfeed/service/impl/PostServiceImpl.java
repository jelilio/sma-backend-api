package io.github.jelilio.smbackend.newsfeed.service.impl;

import io.github.jelilio.smbackend.common.dto.PostDto;
import io.github.jelilio.smbackend.common.dto.response.CloudinaryRes;
import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.github.jelilio.smbackend.common.exception.AlreadyExistException;
import io.github.jelilio.smbackend.common.exception.BadRequestException;
import io.github.jelilio.smbackend.common.exception.NotFoundException;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.common.utils.PaginationUtil;
import io.github.jelilio.smbackend.commonutil.dto.response.PostObject;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.NotificationType;
import io.github.jelilio.smbackend.commonutil.utils.Pair;
import io.github.jelilio.smbackend.newsfeed.client.CloudinaryProxy;
import io.github.jelilio.smbackend.newsfeed.client.ImgurProxy;
import io.github.jelilio.smbackend.newsfeed.entity.Post;
import io.github.jelilio.smbackend.newsfeed.entity.Share;
import io.github.jelilio.smbackend.newsfeed.entity.User;
import io.github.jelilio.smbackend.newsfeed.entity.UserPost;
import io.github.jelilio.smbackend.newsfeed.entity.key.ShareId;
import io.github.jelilio.smbackend.newsfeed.entity.key.UserPostId;
import io.github.jelilio.smbackend.newsfeed.entity.projection.PostPro;
import io.github.jelilio.smbackend.newsfeed.entity.projection.PostReal;
import io.github.jelilio.smbackend.newsfeed.service.CommunityService;
import io.github.jelilio.smbackend.newsfeed.service.NotificationService;
import io.github.jelilio.smbackend.newsfeed.service.PostService;
import io.github.jelilio.smbackend.newsfeed.service.UserService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@ApplicationScoped
public class PostServiceImpl implements PostService {
  private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

  private static final String myPostsPaginationQuery =
      "select" +
          " distinct p1_0.id.postId, p1_0.post.caption, p1_0.post.imageUrl, p1_0.post.imageType, p1_0.post.createdDate, " +
          " o1_0.id, o1_0.name, o1_0.username, o1_0.type, o1_0.avatarUrl, o1_0.verifiedDate, " +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.sharedDate is not null), "  +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.likedDate is not null), " +
          " (select count(*) from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.sharedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.likedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId and f3.id.userId = ?1) " +
          " from UserPost p1_0, User o1_0 where p1_0.post.owner.id = o1_0.id and p1_0.id.userId = ?1 and p1_0.deletedAt IS NULL " +
          " order by p1_0.post.createdDate desc limit ?2 offset ?3";

  private static final String otherPostsPaginationQuery =
      "select" +
          " distinct p1_0.id.postId, p1_0.post.caption, p1_0.post.imageUrl, p1_0.post.imageType, p1_0.post.createdDate, " +
          " o1_0.id, o1_0.name, o1_0.username, o1_0.type, o1_0.avatarUrl, o1_0.verifiedDate, " +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.sharedDate is not null), "  +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.likedDate is not null), " +
          " (select count(*) from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.sharedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.likedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId and f3.id.userId = ?1) " +
          " from UserPost p1_0, User o1_0 where p1_0.post.owner.id = o1_0.id and p1_0.id.userId = ?2 and p1_0.deletedAt IS NULL " +
          " order by p1_0.post.createdDate desc limit ?3 offset ?4";

  private static final String otherPostsNonMemberPaginationQuery =
      "select" +
          " distinct p1_0.id.postId, p1_0.post.caption, p1_0.post.imageUrl, p1_0.post.imageType, p1_0.post.createdDate, " +
          " o1_0.id, o1_0.name, o1_0.username, o1_0.type, o1_0.avatarUrl, o1_0.verifiedDate, " +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.sharedDate is not null), "  +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.likedDate is not null), " +
          " (select count(*) from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.sharedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.likedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId and f3.id.userId = ?1) " +
          " from UserPost p1_0, User o1_0 where p1_0.post.owner.id = o1_0.id and p1_0.id.userId = ?2 and p1_0.post.recipient is null and p1_0.deletedAt IS NULL " +
          " order by p1_0.post.createdDate desc limit ?3 offset ?4";

  private static final String otherPostsMemberPaginationQuery =
      "select" +
          " distinct p1_0.id.postId, p1_0.post.caption, p1_0.post.imageUrl, p1_0.post.imageType, p1_0.post.createdDate, " +
          " o1_0.id, o1_0.name, o1_0.username, o1_0.type, o1_0.avatarUrl, o1_0.verifiedDate, " +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.sharedDate is not null), "  +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.likedDate is not null), " +
          " (select count(*) from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.sharedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.likedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId and f3.id.userId = ?1) " +
          " from UserPost p1_0, User o1_0 where p1_0.post.owner.id = o1_0.id and (p1_0.id.userId = ?2 or p1_0.post.recipient.id = ?2) and p1_0.deletedAt IS NULL " +
          " order by p1_0.post.createdDate desc limit ?3 offset ?4";

  private static final String followingPostsPaginationQuery =
      "select" +
          " distinct p1_0.id.postId, p1_0.post.caption, p1_0.post.imageUrl, p1_0.post.imageType, p1_0.post.createdDate, " +
          " o1_0.id, o1_0.name, o1_0.username, o1_0.type, o1_0.avatarUrl, o1_0.verifiedDate, " +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.sharedDate is not null), "  +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.likedDate is not null), " +
          " (select count(*) from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.sharedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.likedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId and f3.id.userId = ?1) " +
          " from UserPost p1_0, User o1_0 where p1_0.post.owner.id = o1_0.id and p1_0.id.userId in ?2 and p1_0.deletedAt IS NULL " +
          " order by p1_0.post.createdDate desc limit ?3 offset ?4";

  private static final String allPostsPaginationQuery =
      "select " +
          " distinct p1_0.id.postId, p1_0.post.caption, p1_0.post.imageUrl, p1_0.post.imageType, p1_0.post.createdDate, " +
          " o1_0.id, o1_0.name, o1_0.username, o1_0.type, o1_0.avatarUrl, o1_0.verifiedDate,  " +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.sharedDate is not null), "  +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.likedDate is not null), " +
          " (select count(*) from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.sharedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.likedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId and f3.id.userId = ?1) " +
          " from UserPost p1_0, User o1_0 where p1_0.post.owner.id = o1_0.id and p1_0.post.recipient is null and p1_0.deletedAt IS NULL " +
          " order by p1_0.post.createdDate desc limit ?2 offset ?3";

  private static final String allSearchPostsPaginationQuery =
      "select " +
          " distinct p1_0.id.postId, p1_0.post.caption, p1_0.post.imageUrl, p1_0.post.imageType, p1_0.post.createdDate, " +
          " o1_0.id, o1_0.name, o1_0.username, o1_0.type, o1_0.avatarUrl, o1_0.verifiedDate,  " +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.sharedDate is not null), "  +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.likedDate is not null), " +
          " (select count(*) from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.sharedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.likedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId and f3.id.userId = ?1) " +
          " from UserPost p1_0, User o1_0 where p1_0.post.owner.id = o1_0.id and lower(p1_0.post.caption) like ?2 and p1_0.post.recipient is null and p1_0.deletedAt IS NULL " +
          " order by p1_0.post.createdDate desc limit ?3 offset ?4";

  private static final String aPostQuery =
      "select " +
          " distinct p1_0.id.postId, p1_0.post.caption, p1_0.post.imageUrl, p1_0.post.imageType, p1_0.post.createdDate, " +
          " o1_0.id, o1_0.name, o1_0.username, o1_0.type, o1_0.avatarUrl, o1_0.verifiedDate,  " +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.sharedDate is not null), "  +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.likedDate is not null), " +
          " (select count(*) from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.sharedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.likedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId and f3.id.userId = ?1) " +
          " from UserPost p1_0, User o1_0 where p1_0.id.postId = ?2 and p1_0.post.owner.id = o1_0.id and p1_0.deletedAt IS NULL";

  private static final String aOtherPostQuery =
      "select" +
          " distinct p1_0.id.postId, p1_0.post.caption, p1_0.post.imageUrl, p1_0.post.imageType, p1_0.post.createdDate, " +
          " o1_0.id, o1_0.name, o1_0.username, o1_0.type, o1_0.avatarUrl, o1_0.verifiedDate, " +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.sharedDate is not null), "  +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.likedDate is not null), " +
          " (select count(*) from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.sharedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.likedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId and f3.id.userId = ?1) " +
          " from UserPost p1_0, User o1_0 where p1_0.id.postId = ?3 and p1_0.post.owner.id = o1_0.id and p1_0.id.userId = ?2 and p1_0.deletedAt IS NULL ";

  private static final String aOtherPostsNonMemberQuery =
      "select" +
          " distinct p1_0.id.postId, p1_0.post.caption, p1_0.post.imageUrl, p1_0.post.imageType, p1_0.post.createdDate, " +
          " o1_0.id, o1_0.name, o1_0.username, o1_0.type, o1_0.avatarUrl, o1_0.verifiedDate, " +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.sharedDate is not null), "  +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.likedDate is not null), " +
          " (select count(*) from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.sharedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.likedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId and f3.id.userId = ?1) " +
          " from UserPost p1_0, User o1_0 where p1_0.id.postId = ?3 and p1_0.post.owner.id = o1_0.id and p1_0.id.userId = ?2 and p1_0.post.recipient is null and p1_0.deletedAt IS NULL ";

  private static final String aOtherPostsMemberQuery =
      "select" +
          " distinct p1_0.id.postId, p1_0.post.caption, p1_0.post.imageUrl, p1_0.post.imageType, p1_0.post.createdDate, " +
          " o1_0.id, o1_0.name, o1_0.username, o1_0.type, o1_0.avatarUrl, o1_0.verifiedDate, " +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.sharedDate is not null), "  +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.likedDate is not null), " +
          " (select count(*) from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.sharedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.likedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId and f3.id.userId = ?1) " +
          " from UserPost p1_0, User o1_0 where p1_0.id.postId = ?3 and p1_0.post.owner.id = o1_0.id and (p1_0.id.userId = ?2 or p1_0.post.recipient.id = ?2) and p1_0.deletedAt IS NULL ";

  private static final String postRepliesPaginationQuery =
      "select" +
          " distinct p1_0.id.postId, p1_0.post.caption, p1_0.post.imageUrl, p1_0.post.imageType, p1_0.post.createdDate, " +
          " o1_0.id, o1_0.name, o1_0.username, o1_0.type, o1_0.avatarUrl, o1_0.verifiedDate, " +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.sharedDate is not null), "  +
          " (select count(*) from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.likedDate is not null), " +
          " (select count(*) from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.sharedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.id.postId = p1_0.id.postId and f3.id.userId = ?1 and f3.likedDate is not null), " +
          " (select count(*) > 0 from UserPost f3 WHERE f3.replyParentId = p1_0.id.postId and f3.id.userId = ?1) " +
          " from UserPost p1_0, User o1_0 where p1_0.post.owner.id = o1_0.id and p1_0.replyParentId = ?2 and p1_0.deletedAt IS NULL " +
          " order by p1_0.post.createdDate desc limit ?3 offset ?4";

  @Inject
  UserService userService;

  @Inject
  CommunityService communityService;

  @Inject
  NotificationService notificationService;

  @Channel("post-analysis")
  Emitter<PostObject> analysePost;

  @Inject
  @RestClient
  ImgurProxy imgurProxy;

  @Inject
  @RestClient
  CloudinaryProxy cloudinaryProxy;

  @Inject
  Mutiny.SessionFactory sf;

  @ConfigProperty(name = "app.cloudinary.key")
  String cloudinaryKey;

  @ConfigProperty(name = "app.cloudinary.preset")
  String cloudinaryPreset;

//  public PostServiceImpl() {
//    imgurProxy = QuarkusRestClientBuilder.newBuilder()
//        .baseUri(URI.create("https://api.imgur.com/3"))
//        .clientHeadersFactory(ClientHeadersFactory.class)
//        .build(ImgurProxy.class);
//  }

  @Override
  public Uni<Post> findByIdOrNull(UUID id) {
    return Post.findById(id);
  }

  @Override
  public Uni<Post> findById(String id) {
    return Post.findById(id).onItem().ifNull()
        .failWith(() -> new NotFoundException("Not found"));
  }

  public Uni<UserPost> findById(String userId, String postId) {
    return UserPost.findById(userId, postId).onItem().ifNull()
        .failWith(() -> new NotFoundException("Not found"));
  }

  public Uni<UserPost> findByIdAndShared(String userId, String postId) {
    return UserPost.findByIdAndShared(userId, postId).onItem().ifNull()
        .failWith(() -> {
          logger.info("not found");
          return new NotFoundException("Not found");
        });
  }

  public Uni<UserPost> findByIdAndLiked(String userId, String postId) {
    return UserPost.findByIdAndLiked(userId, postId).onItem().ifNull()
        .failWith(() -> {
          return new NotFoundException("Not found");
        });
  }

  @Override
  public Uni<PostPro> findPostById(String userId, String id) {
    Uni<User> userUni = userService.findById(userId);

    return userUni.flatMap(user -> {
      return sf.withSession(session -> {
        var query = session
            .createQuery(aPostQuery, PostPro.class)
            .setParameter(1, user.id) // loggedInUserId
            .setParameter(2, UUID.fromString(id)); //postId
        return query.getSingleResultOrNull().onItem().ifNull().failWith(new NotFoundException("Nothing found"));
      });
    });
  }

  @Override
  public Uni<Paged<PostReal>> findAll(int size, int index) {
    logger.info("findAll: size: {}, index: {}", size, index);

    Page page = Page.of(index, size);

    var query = Post.findAllPosts(size, index*size).project(PostReal.class);
    var count = Post.countAllPosts();

    return PaginationUtil.paginate(page, query, count);
  }

  @Override
  @Deprecated
  public Uni<List<Post>> findAll(String userId) {
    Uni<User> userUni = userService.findById(userId);
    Uni<List<User>> followingUni = userService.followings(userId);

    return userUni.flatMap(user -> followingUni
        .flatMap(following -> {
          Set<User> users = new HashSet<>(following);
          users.add(user);

          logger.debug("following: {}", following);
          logger.debug("users: {}", users);

          return Post.find(users).list();
        })
    );
  }

  @Override
  public Uni<Paged<PostPro>> findAll(String userId, int size, int index) {
    Page page = Page.of(index, size);
    Uni<User> userUni = userService.findById(userId);

    return userUni.flatMap(user -> {
      Uni<Long> count = UserPost.countAllPost();
      Uni<List<PostPro>> list =  sf.withSession( session -> {
        var query = session
            .createQuery(allPostsPaginationQuery, PostPro.class)
            .setParameter(1, user.id)
            .setParameter(2, size)
            .setParameter(3, index*size);

        return query.getResultList();
      });

      return PaginationUtil.paginate(page, list, count);
    });
  }

  @Override
  public Uni<Paged<PostPro>> searchAll(String userId, String text, int size, int index) {
    Page page = Page.of(index, size);
    Uni<User> userUni = userService.findById(userId);

    return userUni.flatMap(user -> {
      var q = "%" + text.toLowerCase() + "%";
      Uni<Long> count = UserPost.countAllPost(q);
      Uni<List<PostPro>> list =  sf.withSession( session -> {
        var query = session
            .createQuery(allSearchPostsPaginationQuery, PostPro.class)
            .setParameter(1, user.id)
            .setParameter(2, q)
            .setParameter(3, size)
            .setParameter(4, index*size);

        return query.getResultList();
      });

      return PaginationUtil.paginate(page, list, count);
    });
  }

  @Override
  public Uni<Paged<PostPro>> findFollowed(String userId, int size, int index) {
    Page page = Page.of(index, size);
    Uni<User> userUni = userService.findById(userId);
    Uni<List<User>> followingUni = userService.followings(userId);

    return userUni.flatMap(user -> followingUni
        .flatMap(following -> {
          Set<UUID> userIds = following.stream().map(it -> it.id)
              .collect(Collectors.toSet());
          userIds.add(user.id);

          logger.debug("findFollowed: following: {}", following);
          logger.debug("findFollowed: users: {}", userIds);

          Uni<Long> count = UserPost.countFollowingPost(userIds);
          Uni<List<PostPro>> list =  sf.withSession( session -> {
            var query = session
                .createQuery(followingPostsPaginationQuery, PostPro.class)
                .setParameter(1, user.id)
                .setParameter(2, userIds)
                .setParameter(3, size)
                .setParameter(4, index*size);
            return query.getResultList();
          });

          return PaginationUtil.paginate(page, list, count);
        })
    );
  }

  @Override
  public Uni<Paged<PostPro>> findFollowedCommunity(String userId, int size, int index) {
    Page page = Page.of(index, size);
    Uni<User> userUni = userService.findById(userId);
    Uni<List<User>> followingUni = userService.followingCommunities(userId);

    return userUni.flatMap(user -> followingUni
        .flatMap(following -> {
          Set<UUID> userIds = following.stream().map(it -> it.id)
              .collect(Collectors.toSet());
          userIds.add(user.id);

          logger.debug("findFollowed: following: {}", following);
          logger.debug("findFollowed: users: {}", userIds);

          Uni<Long> count = UserPost.countFollowingPost(userIds);
          Uni<List<PostPro>> list =  sf.withSession( session -> {
            var query = session
                .createQuery(followingPostsPaginationQuery, PostPro.class)
                .setParameter(1, user.id)
                .setParameter(2, userIds)
                .setParameter(3, size)
                .setParameter(4, index*size);
            return query.getResultList();
          });

          return PaginationUtil.paginate(page, list, count);
        })
    );
  }

  @Override
  public Uni<Paged<PostPro>> findMyPosts(String userId, int size, int index) {
    logger.info("findMyPosts, hql; {}", userId);
    Page page = Page.of(index, size);

    Uni<User> userUni = userService.findById(userId);

    return userUni.flatMap(user -> {
      Uni<Long> count = UserPost.countMyPost(user);
      Uni<List<PostPro>> list =  sf.withSession( session -> {
        var query = session
            .createQuery(myPostsPaginationQuery, PostPro.class)
            .setParameter(1, user.id)
            .setParameter(2, size)
            .setParameter(3, index*size);
        return query.getResultList();
      });

      return PaginationUtil.paginate(page, list, count);
    });
  }

//  @Override
  public Uni<Paged<PostPro>> findOtherPosts(User loggedInUser, User user, int size, int index) {
    logger.info("findOtherPosts, hql; {}", user);
    Page page = Page.of(index, size);

    Uni<Long> count = UserPost.countOtherPost(user);
    Uni<List<PostPro>> list =  sf.withSession( session -> {
      var query = session
          .createQuery(otherPostsPaginationQuery, PostPro.class)
          .setParameter(1, loggedInUser.id)
          .setParameter(2, user.id)
          .setParameter(3, size)
          .setParameter(4, index*size);
      return query.getResultList();
    });

    return PaginationUtil.paginate(page, list, count);
  }

  public Uni<PostPro> findAOtherPost(User loggedInUser, User user, UUID postId) {
    logger.info("findAOtherPost, hql; {}", user);

    return sf.withSession( session -> {
      var query = session
          .createQuery(aOtherPostQuery, PostPro.class)
          .setParameter(1, loggedInUser.id)
          .setParameter(2, user.id)
          .setParameter(3, postId);
      return query.getSingleResultOrNull().onItem().ifNull()
          .failWith(new NotFoundException("Nothing found"));
    });
  }

  public Uni<Paged<PostPro>> findOtherNonMemberPosts(User loggedInUser, User user, int size, int index) {
    logger.info("findOtherNonMemberPosts, hql; {}", user);
    Page page = Page.of(index, size);

    var count = UserPost.countOtherNonMemberPost(user);
    var list =  sf.withSession( session -> {
      var query = session
          .createQuery(otherPostsNonMemberPaginationQuery, PostPro.class)
          .setParameter(1, loggedInUser.id)
          .setParameter(2, user.id)
          .setParameter(3, size)
          .setParameter(4, index*size);
      return query.getResultList();
    });

    return PaginationUtil.paginate(page, list, count);
  }

  public Uni<PostPro> findAOtherNonMemberPosts(User loggedInUser, User user, UUID postId) {
    logger.info("findAOtherNonMemberPosts, hql; {}", user);

    return sf.withSession( session -> {
      var query = session
          .createQuery(aOtherPostsNonMemberQuery, PostPro.class)
          .setParameter(1, loggedInUser.id)
          .setParameter(2, user.id)
          .setParameter(3, postId);
      return query.getSingleResultOrNull().onItem().ifNull()
          .failWith(new NotFoundException("Nothing found"));
    });
  }

  public Uni<Paged<PostPro>> findOtherMemberPosts(User loggedInUser, User user, int size, int index) {
    logger.info("findOtherMemberPosts, hql; {}", user);
    Page page = Page.of(index, size);

    var count = UserPost.countOtherMemberPost(user);
    var list =  sf.withSession( session -> {
      var query = session
          .createQuery(otherPostsMemberPaginationQuery, PostPro.class)
          .setParameter(1, loggedInUser.id)
          .setParameter(2, user.id)
          .setParameter(3, size)
          .setParameter(4, index*size);
      return query.getResultList();
    });

    return PaginationUtil.paginate(page, list, count);
  }

  public Uni<PostPro> findAOtherMemberPost(User loggedInUser, User user, UUID postId) {
    logger.info("findOtherMemberPosts, hql; {}", user);

    return sf.withSession( session -> {
      var query = session
          .createQuery(aOtherPostsMemberQuery, PostPro.class)
          .setParameter(1, loggedInUser.id)
          .setParameter(2, user.id)
          .setParameter(3, postId);
      return query.getSingleResultOrNull().onItem().ifNull()
          .failWith(new NotFoundException("Nothing found"));
    });
  }


  // find other user post and community/club's posts by member
  @Override
  public Uni<Paged<PostPro>> findOtherUserPosts(String userId, String otherUserId, int size, int index) {
    Page page = Page.of(index, size);
    Uni<User> userUni = userService.findById(userId);
    Uni<User> otherUserUni = userService.findById(otherUserId);

    return userUni.flatMap(user -> {
      return otherUserUni.flatMap(otherUser -> {
        if(otherUser.type != UserType.COMMUNITY && otherUser.type != UserType.CLUB) {
          return findOtherPosts(user, otherUser, size, index);
        }

        return communityService.checkIfAlreadyAMember(otherUser, user).flatMap(isMember -> {
          if(!isMember) {
            return findOtherNonMemberPosts(user, otherUser, size, index);
          }

          return findOtherMemberPosts(user, otherUser, size, index);
        });
      });
    });
  }

  @Override
  public Uni<PostPro> findPostById(String userId, String otherUserId, String postId) {
    Uni<User> userUni = userService.findById(userId);
    Uni<User> otherUserUni = userService.findById(otherUserId);

    return userUni.flatMap(user -> {
      return otherUserUni.flatMap(otherUser -> {
        if(otherUser.type != UserType.COMMUNITY && otherUser.type != UserType.CLUB) {
          return findAOtherPost(user, otherUser, UUID.fromString(postId));
        }

        return communityService.checkIfAlreadyAMember(otherUser, user).flatMap(isMember -> {
          if(!isMember) {
            return findAOtherNonMemberPosts(user, otherUser, UUID.fromString(postId));
          }

          return findAOtherMemberPost(user, otherUser, UUID.fromString(postId));
        });
      });
    });
  }

  @Deprecated(forRemoval = true)
  public Uni<Paged<Post>> findUserPosts(String userId, int size, int index) {
    Page page = Page.of(index, size);
    Uni<User> userUni = userService.findById(userId);

    return userUni.flatMap(user -> PaginationUtil
        .paginate(page, Post.ownerPosts(user).page(page)));
  }

  public Uni<Boolean> alreadyShared22(UserPostId shareId) {
    return UserPost.alreadyShared(shareId).map(count -> count > 0);
  }

  public Uni<Boolean> alreadyLiked(UserPostId shareId) {
    return UserPost.alreadyLiked(shareId).map(count -> count > 0);
  }

  public Uni<Pair<Boolean, UserPost>> checkIfAlreadyShared22(User user, Post post) {
    if(post.owner.equals(user)) {
      return Uni.createFrom().failure(() -> new AlreadyExistException("You owned this post"));
    }

    Uni<UserPost> userPostUni = UserPost.findById((new UserPost(user, post)).id);
    return userPostUni.map(userPost -> {
      if(userPost == null) {
        return Pair.of(false, null);
      }

      return Pair.of(userPost.sharedDate != null, userPost);
    });

//    return alreadyShared22((new UserPost(user, post)).id);
  }

  public Uni<Pair<Boolean, UserPost>> checkIfAlreadyLiked(User user, Post post) {
    if(post.owner.equals(user)) {
      return Uni.createFrom().failure(() -> new AlreadyExistException("You owned this post"));
    }

    Uni<UserPost> userPostUni = UserPost.findById((new UserPost(user, post)).id);
    return userPostUni.map(userPost -> {
      if(userPost == null) {
        return Pair.of(false, null);
      }

      return Pair.of(userPost.likedDate != null, userPost);
    });

//    return alreadyLiked((new UserPost(user, post)).id);
  }


  BiFunction<User, Post, Uni<Post>> liked = (User user, Post likedPost) -> {
    return checkIfAlreadyLiked(user, likedPost)
        .flatMap(shared -> {
          if(shared.left()) {
            return Uni.createFrom().item(likedPost);
          }

          UserPost newPost =
              shared.right() == null? UserPost.like(user, likedPost) :
                  shared.right().like();

          return Panache.<UserPost>withTransaction(newPost::persist)
              .map(__ -> likedPost)
              .flatMap(createdPost -> notificationService.createNotification(NotificationType.LIKE, user, createdPost))
              .map(__ -> likedPost);
        });
  };

  BiFunction<User, Post, Uni<Post>> shared = (User user, Post sharedPost) -> {
    return checkIfAlreadyShared22(user, sharedPost)
        .flatMap(shared -> {
          if(shared.left()) {
            return Uni.createFrom().item(sharedPost);
          }

          UserPost newPost =
              shared.right() == null? UserPost.share(user, sharedPost) :
              shared.right().share();

          return Panache.<UserPost>withTransaction(newPost::persist)
              .map(__ -> sharedPost)
              .flatMap(createdPost -> notificationService.createNotification(NotificationType.SHARE, user, createdPost))
              .map(__ -> sharedPost);
        });
  };

  private Uni<Post> shareOrLike(String userId, String postId, BiFunction<User, Post, Uni<Post>> sharedOrLiked) {
    Uni<Post> postUni = findById(postId);
    Uni<User> userUni = userService.findById(userId);

    return Panache.withTransaction(() -> {
      return userUni.flatMap(user -> {
        return postUni.flatMap(post -> {
          return sharedOrLiked.apply(user, post);
        });
      });
    });
  }

  @Override
  public Uni<Post> share(String userId, String postId) {
    return shareOrLike(userId, postId, shared);
  }

  @Override
  public Uni<Post> like(String userId, String postId) {
    return shareOrLike(userId, postId, liked);
  }


//  @Override
  public Uni<Post> share_(String userId, String postId) {
    Uni<Post> postUni = findById(postId);
    Uni<User> userUni = userService.findById(userId);

    return Panache.withTransaction(() -> {
      return userUni.flatMap(user -> {
        return postUni.flatMap(post -> {
          Post sharedPost = post.originalPost != null?
              post.originalPost : post;

          Post newPost = new Post(sharedPost, user); // create a copy
//          sharedPost.sharedCount += sharedPost.sharedCount + 1; // not useful, to be removed

          return checkIfAlreadyShared(user, sharedPost)
//              .flatMap(__ -> Panache.<Post>withTransaction(sharedPost::persist))
              .flatMap(isShared -> {
                if(isShared) {
                  // un-share the post
                  var share = new Share(user, sharedPost);
                  return Share.findById(share.id).flatMap(itExist -> {
                    return delete(userId, postId).flatMap(__ -> {
                      return itExist.delete().map((Void) -> sharedPost);
                    });
                  });
                }

                return Panache.<Post>withTransaction(sharedPost::persist);
              })
              .flatMap(createdPost -> notificationService.createNotification(NotificationType.SHARE, user, createdPost))
              .flatMap(__ -> Panache.withTransaction(newPost::persist));
        });
      });
    });
  }

  @Override
  public Uni<Post> unshared(String userId, String postId) {
    logger.info("unshared: userId: {}, postId: {}", userId, postId);
    Uni<UserPost> userPostUni = findByIdAndShared(userId, postId);

    return Panache.withTransaction(() -> {
      return userPostUni.flatMap(userPost -> {
        if(userPost.user.equals(userPost.post.owner)) {
          userPost.sharedDate = null;
        } else {
          if(userPost.isSharedOnly()) {
            return userPost.delete()
                .map(__ -> userPost.post);
          } else {
            userPost.sharedDate = null;
          }
        }
        return Panache.<UserPost>withTransaction(userPost::persist)
            .map(updated -> updated.post);
      });
    });
  }

  @Override
  public Uni<Post> unliked(String userId, String postId) {
    logger.info("unliked: userId: {}, postId: {}", userId, postId);
    Uni<UserPost> userPostUni = findByIdAndLiked(userId, postId);

    return Panache.withTransaction(() -> {
      return userPostUni.flatMap(userPost -> {
        if(userPost.user.equals(userPost.post.owner)) {
          userPost.likedDate = null;
        } else {
          if(userPost.isLikedOnly()) {
            return userPost.delete()
                .map(__ -> userPost.post);
          } else {
            userPost.likedDate = null;
          }
        }
        return Panache.<UserPost>withTransaction(userPost::persist)
            .map(updated -> updated.post);
      });
    });
  }

  private Uni<Post> memberCanPostOrReply(User loggedInUser, User recipient, Post post) {
    if(recipient == null) {
      return Panache.<Post>withTransaction(post::persist);
    }

    if(recipient.type != UserType.COMMUNITY && recipient.type != UserType.CLUB) {
      post.recipient = recipient;
      return Panache.<Post>withTransaction(post::persist);
    }

    return communityService.checkIfAlreadyAMember(recipient, loggedInUser).flatMap(isMember -> {
      if(!isMember) {
        return Uni.createFrom().failure(new BadRequestException("Member only"));
      }

      post.recipient = recipient;
      return Panache.<Post>withTransaction(post::persist);
    });
  }

  @Override
  public Uni<Post> create(String userId, PostDto postDto) {
    Uni<User> userUni = userService.findById(userId);
    Uni<User> recipientUni = postDto.recipientId == null? Uni.createFrom().nullItem() :
        userService.findById(postDto.recipientId);

    return Panache.withTransaction(() ->
        userUni.flatMap(user -> {
          return recipientUni.flatMap(recipient -> {
            Uni<CloudinaryRes> uploadImage = postDto.file() != null?
                imageUploader(postDto.file()) : Uni.createFrom().nullItem();

            return uploadImage.flatMap(imgurResponse -> {
              Post post = new Post(postDto.caption(), user);

              if(imgurResponse != null) {
                post.imageUrl = imgurResponse.secureUrl();
                post.imageType = imgurResponse.getType();
              }

              if(recipient != null) {
                return memberCanPostOrReply(user, recipient, post);
              }

              // Community or Club post are self-recipient by default
              if ((user.type == UserType.COMMUNITY || user.type == UserType.CLUB) && !postDto.everyone) {
                post.recipient = user;
              }

              return Panache.<Post>withTransaction(post::persist);
            }).flatMap(createdPost -> {
              var userPost = new UserPost(user, createdPost);
              return Panache.<UserPost>withTransaction(userPost::persist)
                  .flatMap(__ -> Uni.createFrom().item(createdPost));
            }).flatMap(createdPost -> {
              if(createdPost.recipient == null) {
                return Uni.createFrom().item(createdPost);
              }
              // if owner of the post is community or club, the community or club is already a recipient
              if (user.type == UserType.COMMUNITY || user.type == UserType.CLUB) {
                return Uni.createFrom().item(createdPost);
              }

              var recipientPost = new UserPost(createdPost.recipient , createdPost);
              return Panache.<UserPost>withTransaction(recipientPost::persist)
                  .flatMap(__ -> Uni.createFrom().item(createdPost));
            }).flatMap(this::analysePost);
          });
        })
    );
  }

  private Uni<Post> analysePost(Post createdPost) {
    logger.info("about to send post: {}, for analysis", createdPost.id);
    analysePost.send(new PostObject(createdPost.id, createdPost.caption, createdPost.imageUrl, createdPost.owner.id));
    return Uni.createFrom().item(createdPost);
  }

  //  @Override
  public Uni<Post> create_(String userId, PostDto postDto) {
    Uni<User> userUni = userService.findById(userId);
    Uni<User> recipientUni = postDto.recipientId == null? Uni.createFrom().nullItem() :
        userService.findById(postDto.recipientId);

    return Panache.withTransaction(() ->
        userUni.flatMap(user -> {
          return recipientUni.flatMap(recipient -> {
            Uni<CloudinaryRes> uploadImage = postDto.file() != null?
                imageUploader(postDto.file()) : Uni.createFrom().nullItem();

            return uploadImage.flatMap(imgurResponse -> {
              Post post = new Post(postDto.caption(), user);

              if(imgurResponse != null) {
                post.imageUrl = imgurResponse.secureUrl();
                post.imageType = imgurResponse.getType();
              }

              post.recipient = recipient;

              // Community or Club post are self-recipient by default
              if ((user.type == UserType.COMMUNITY || user.type == UserType.CLUB) && !postDto.everyone) {
                post.recipient = user;
              }

              return Panache.<Post>withTransaction(post::persist);
            }).flatMap(this::analysePost);
          });
        })
    );
  }

  @Override
  public Uni<Post> reply(String userId, String parentPostId, PostDto postDto) {
    Uni<User> userUni = userService.findById(userId);
    Uni<Post> postUni = findById(parentPostId);

    return Panache.withTransaction(() ->
       userUni.flatMap(user -> postUni.flatMap(
           parentPost -> {
             Post post = new Post(postDto.caption(), parentPost, user);

             final User recipient = parentPost.recipient;

             if(recipient != null) {
               return memberCanPostOrReply(user, recipient, post);
             }

             return Panache.<Post>withTransaction(post::persist);
           }).flatMap(repliedPost -> {
             var userPost = new UserPost(user, repliedPost, repliedPost.parentPost);
             return Panache.<UserPost>withTransaction(userPost::persist)
                 .flatMap(__ -> Uni.createFrom().item(repliedPost));
           }).flatMap(this::analysePost)
       )
    );
  }

  @Override
  public Uni<Long> delete(String userId, String postId) {
    Uni<User> userUni = userService.findById(userId);

    return Panache.withTransaction(() ->
        userUni.flatMap(user -> UserPost.deleteByPost(postId, user)));
  }

  @Override
  public Uni<Long> deleteByPost(String userId, String postId) {
    Uni<User> userUni = userService.findById(userId);

    return Panache.withTransaction(() ->
        userUni.flatMap(user -> Post.delete(postId, user)
            .flatMap(__ -> UserPost.deleteByPost(postId, user))));
  }

  @Deprecated
  public Uni<Paged<PostReal>> findPostReplies_old(String userId, String postId, int size, int index) {
    logger.info("findPostReplies: {}", userId);

    Page page = Page.of(index, size);
    Uni<Post> postUni = findById(postId);
    Uni<User> userUni = userService.findById(userId);

    return postUni.flatMap(post -> {
      return userUni.flatMap(user -> {
        var query = Post.findAllPostReplies(user.id, post.id, size, index*size).project(PostReal.class);
        var count = Post.countAllPostReplies(post.id);

        return PaginationUtil.paginate(page, query, count);
      });
    });

  }

  @Override
  public Uni<Paged<PostPro>> findPostReplies(String userId, String postId, int size, int index) {
    logger.info("findPostReplies22, hql; {}", userId);
    Page page = Page.of(index, size);

    Uni<Post> postUni = findById(postId);
    Uni<User> userUni = userService.findById(userId);

    return postUni.flatMap(post -> {
      return userUni.flatMap(loggedInUser -> {
        Uni<Long> count = UserPost.countPostReply(post.id);

        Uni<List<PostPro>> list =  sf.withSession( session -> {
          var query = session
              .createQuery(postRepliesPaginationQuery, PostPro.class)
              .setParameter(1, loggedInUser.id)
              .setParameter(2, post.id)
              .setParameter(3, size)
              .setParameter(4, index*size);
          return query.getResultList();
        });

        return PaginationUtil.paginate(page, list, count);
      });
    });
  }

  public Uni<Boolean> alreadyShared(ShareId shareId) {
    return Share.countById(shareId).map(count -> count > 0);
  }

  public Uni<Boolean> checkIfAlreadyShared(User user, Post post) {
    if(post.owner.equals(user)) {
      return Uni.createFrom().failure(() -> new AlreadyExistException("You owned this post"));
    }

    var share = new Share(user, post);
//
//    if(post.originalPost != null && post.owner.equals(user)) {
//      return Uni.createFrom().failure(() -> new AlreadyExistException("This is already a shared post by you"));
//    }

    return alreadyShared(share.id).flatMap(isShared -> {
      if(isShared) {
        return Uni.createFrom().item(true);
//        return Uni.createFrom().failure(() -> new AlreadyExistException("This is already a shared post by you"));
      }

      return Panache.withTransaction(share::persist).map(__ -> false);
    });
//    return Post.isShared(user, post)
//        .flatMap(count -> {
//          if (count > 0) {
//            return Uni.createFrom().failure(() -> new AlreadyExistException("You already share this post"));
//          }
//
//          return Uni.createFrom().item(false);
//        });
  }

  public Uni<CloudinaryRes> imageUploader(File image) {
    return cloudinaryProxy.sendMultipartData(image, cloudinaryKey, cloudinaryPreset);
  }
}
