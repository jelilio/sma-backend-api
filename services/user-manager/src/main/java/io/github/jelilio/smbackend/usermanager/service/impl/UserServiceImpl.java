package io.github.jelilio.smbackend.usermanager.service.impl;

import com.poiji.bind.Poiji;
import com.poiji.exception.PoijiExcelType;
import io.github.jelilio.smbackend.common.dto.*;
import io.github.jelilio.smbackend.common.dto.response.*;
import io.github.jelilio.smbackend.common.entity.enumeration.AccountStatus;
import io.github.jelilio.smbackend.common.entity.enumeration.DateQueryType;
import io.github.jelilio.smbackend.common.entity.enumeration.UserStatus;
import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.github.jelilio.smbackend.common.exception.AlreadyExistException;
import io.github.jelilio.smbackend.common.exception.AuthenticationException;
import io.github.jelilio.smbackend.common.exception.BadRequestException;
import io.github.jelilio.smbackend.common.exception.NotFoundException;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.common.utils.PaginationUtil;
import io.github.jelilio.smbackend.common.utils.ValidatorUtil;
import io.github.jelilio.smbackend.usermanager.client.cloudinary.CloudinaryProxy;
import io.github.jelilio.smbackend.usermanager.client.imgur.ImgurProxy;
import io.github.jelilio.smbackend.usermanager.client.keycloak.ClientProxy;
import io.github.jelilio.smbackend.usermanager.client.keycloak.UserProxy;
import io.github.jelilio.smbackend.usermanager.dto.UserDto;
import io.github.jelilio.smbackend.usermanager.entity.Course;
import io.github.jelilio.smbackend.usermanager.entity.Institution;
import io.github.jelilio.smbackend.usermanager.entity.School;
import io.github.jelilio.smbackend.usermanager.entity.User;
import io.github.jelilio.smbackend.usermanager.model.*;
import io.github.jelilio.smbackend.usermanager.queue.model.RegisterUser;
import io.github.jelilio.smbackend.usermanager.repository.UserRepository;
import io.github.jelilio.smbackend.usermanager.service.*;
import io.github.jelilio.smbackend.usermanager.utils.RandomUtil;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.jelilio.smbackend.common.exception.AuthenticationException.*;

@ApplicationScoped
public class UserServiceImpl implements UserService {
  private static final String NEW_USER_ROLE_GRP = "%s-USER_GRP";
  private static final String NEW_USER_ALM_ROLE_GRP = "%s-USER_ALM_GRP";
  private static final String NEW_USER_COM_ROLE_GRP = "%s-USER_COM_GRP";
  private static final String NEW_USER_CLB_ROLE_GRP = "%s-USER_CLB_GRP";
  private static final String NEW_USER_STF_ROLE_GRP = "%s-USER_STF_GRP";
  private static final String DEF_REGISTERED_CLIENT = "newsfeed";

  private static final Map<UserType, String> userTypeStringMap = Map.of(
      UserType.STUDENT, NEW_USER_ROLE_GRP,
      UserType.COMMUNITY, NEW_USER_COM_ROLE_GRP,
      UserType.CLUB, NEW_USER_CLB_ROLE_GRP,
      UserType.STAFF, NEW_USER_STF_ROLE_GRP,
      UserType.ALUMNI, NEW_USER_ALM_ROLE_GRP
  );

  private static final Map<DateQueryType, Integer> userQueryTypeMap = Map.of(
      DateQueryType.LAST7DAYS, 7,
      DateQueryType.LAST30DAYS, 30,
      DateQueryType.LAST90DAYS, 90,
      DateQueryType.ALL, -1
  );

  private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

  @Inject
  @RestClient
  UserProxy userProxy;

  @Inject
  @RestClient
  ClientProxy clientProxy;

  @Inject
  RoleService roleService;

  @Inject
  SchoolService schoolService;

  @Inject
  CourseService courseService;

  @Inject
  InstitutionService institutionService;

  @Inject
  UserRepository userRepository;

  @Channel("user-registered")
  Emitter<RegisterRes> userRegister;

//  @Channel("user-updated")
//  Emitter<UserUpdatedRes> userUpdatedResEmitter;

  @Channel("user-oidc-registered")
  Emitter<RegisterUser> userOidcRegister;

  @Inject
  ReactiveRedisDataSource reactiveRedisClient;

  @Inject
  RandomUtil randomUtil;

  @Inject
  MailerService mailerService;

  @Inject
  @RestClient
  ImgurProxy imgurProxy;

  @Inject
  @RestClient
  CloudinaryProxy cloudinaryProxy;

  @ConfigProperty(name = "app.username.blacklist")
  String[] usernameBlacklist;

  @ConfigProperty(name = "app.username.default-password")
  String usernameDefaultPassword;

  @ConfigProperty(name = "jwt-auth-otp.otp.duration")
  Long otpKeyDuration;

  @ConfigProperty(name = "app.cloudinary.key")
  String cloudinaryKey;

  @ConfigProperty(name = "app.cloudinary.preset")
  String cloudinaryPreset;

  public Uni<Boolean> checkIfIdNumberIsUsed(String idNumber) {
    return userRepository.countByIdNumber(idNumber)
        .onItem().transform(count -> count > 0);
  }

  public Uni<Boolean> checkIfIdNumberIsUsedButNotId(String id, String idNumber) {
    return userRepository.countByIdNumberButNotId(id, idNumber)
        .onItem().transform(count -> count > 0);
  }

  public Uni<Boolean> checkIfUsernameIsUsed(String idNumber) {
    return userRepository.countByIdNumber(idNumber)
        .onItem().transform(count -> count > 0);
  }

  public Uni<Boolean> checkIfUsernameIsUsedButNotId(String id, String username) {
    return userRepository.countByUsernameButNotId(id, username)
        .onItem().transform(count -> count > 0);
  }

  public Uni<Boolean> checkIfEmailIsUsed(String email) {
    return userRepository.countByEmail(email)
        .onItem().transform(count -> count > 0);
  }

  public Uni<Boolean> checkIfEmailIsUsedButNotId(String id, String email) {
    return userRepository.countByEmailNotId(id, email)
        .onItem().transform(count -> count > 0);
  }

  @Override
  public Uni<User> findById(String id) {
    return userRepository.findById(id).onItem().ifNull()
        .failWith(() -> new NotFoundException("User not found"));
  }

  @Override
  public Uni<User> findByOidcId(String id) {
    return userRepository.findByOidcId(id).onItem().ifNull()
        .failWith(() -> new NotFoundException("User not found"));
  }

  @Override
  public Uni<List<User>> findAll() {
    return  userRepository.findAll().list();
  }

  @Override
  public Uni<List<User>> findAllByQueryType(DateQueryType queryType) {
    Instant startDate = null;

    int lastDays = userQueryTypeMap.getOrDefault(queryType, -1);

    if(lastDays > 0) {
      startDate = ZonedDateTime.now().minusDays(lastDays).toInstant();
    }

    if (startDate == null) return findAll();

    return userRepository.findAllByCreatedAfterDate(startDate).list();
  }

  @Override
  public Uni<Paged<User>> findAll(int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, userRepository.findAll(Sort.descending("createdDate")).page(page));
  }

  @Override
  public Uni<List<User>> findAllByType(UserType type) {
    return userRepository.findAllByType(type).list();
  }

  @Override
  public Uni<List<User>> findAllByTypeAndQueryType(UserType type, DateQueryType queryType) {
    Instant startDate = null;

    int lastDays = userQueryTypeMap.getOrDefault(queryType, -1);

    if(lastDays > 0) {
      startDate = ZonedDateTime.now().minusDays(lastDays).toInstant();
    }

    if (startDate == null) return findAllByType(type);

    return userRepository.findAllByTypeAndCreatedAfterDate(type, startDate).list();
  }

  @Override
  public Uni<Paged<User>> findAllByType(UserType type, int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, userRepository.findAllByType(type).page(page));
  }

  public Uni<RegisterRes> register(RegisterDto dto, Set<String> roles, UserType type, String newUserRoleGroup) {
    return register(
        new Register(dto.email(), dto.name(), false, false, dto.password()),
        roles, type, newUserRoleGroup
    );
  }

  private Uni<RegisterRes> register(String email, RegisterCommunityDto dto, Set<String> roles, UserType type, String newUserRoleGroup) {
    return register(
        new RegisterWithUsername(email, dto.username(), dto.name(), true, false, dto.password()),
        roles, type, newUserRoleGroup
    );
  }

  private Uni<RegisterRes> register(Register register, Set<String> roles, UserType type, String newUserRoleGroup) {
    logger.info("register, about to AIM register: {}", register);
    return userProxy.register(register).flatMap(it -> register(it, roles, type, newUserRoleGroup));
  }

  private Uni<RegisterRes> register(RegisterWithUsername register, Set<String> roles, UserType type, String newUserRoleGroup) {
    logger.info("RegisterWithUsername, about to AIM register: {}", register);
    return userProxy.register(register).flatMap(it -> register(it, roles, type, newUserRoleGroup));
  }

  private Uni<RegisterRes> register(Response it, Set<String> roles, UserType type, String newUserRoleGroup) {
    logger.info("register, after AIM register: {}", type);
    var location = it.getHeaderString("Location");
    var userId = location != null? location.substring(location.lastIndexOf("/")+1) : null;

    return roleService.convertToActualRoles(roles).collect().asList()
        .flatMap(list -> userProxy.assignRolesToUser(userId, new HashSet<>(list))) // assign user to realm roles
        .flatMap(list -> addUserToAppRole(userId, DEF_REGISTERED_CLIENT, newUserRoleGroup)) // assign user to specific client (app) roles
        .flatMap(response -> getUser(userId))
        .flatMap(user -> {
          logger.info("after fetching user created: {}", user);
          return save(new RegisterRes(user.id, user.firstName, user.lastName, user.email, user.username, false, type), type);
        }) // save the registered user to db
        .map(user -> {
          var res = new RegisterRes(user.oidcId.toString(), user.name, user.email, user.username, user.enabled, user.type);
          userRegister.send(res); // broadcast the registered user to other services "waiting" for the user
          return res;
        });
  }

  public Uni<RegisterRes> registerAndUpdate(User user, String password, Set<String> roles, UserType type, String newUserRoleGroup) {
    return userProxy.register(new Register(user.email, user.name, user.enabled, user.isClaimed(), password)).flatMap(it -> {
      var location = it.getHeaderString("Location");
      var userId = location != null? location.substring(location.lastIndexOf("/")+1) : null;

      return roleService.convertToActualRoles(roles).collect().asList()
          .flatMap(list -> userProxy.assignRolesToUser(userId, new HashSet<>(list))) // assign user to realm roles
          .flatMap(list -> addUserToAppRole(userId, DEF_REGISTERED_CLIENT, newUserRoleGroup)) // assign user to specific client (app) roles
          .flatMap(response -> getUser(userId))
          .flatMap(kUser -> update(user, kUser)) // save the registered user to db
          .map(extUser -> {
            var res = new RegisterRes(extUser.oidcId.toString(), extUser.name, extUser.email, extUser.username, extUser.enabled, extUser.type);
            userRegister.send(res); // broadcast the registered user to other services "waiting" for the user
            return res;
          });
    });
  }

  private Uni<User> update(User user, KUser kUser) {
    user.oidcId = UUID.fromString(kUser.id);
    return Panache.withTransaction(user::persist);
  }

  @Override
  public Uni<KUser> registerOnOIDC(RegisterDto dto, Set<String> roles, UserType type) {
    String newUserRoleGroup = userTypeStringMap.getOrDefault(type, null);

    return userProxy.register(new Register(dto.email(), dto.name(), dto.password())).flatMap(it -> {
      var location = it.getHeaderString("Location");
      var userId = location != null? location.substring(location.lastIndexOf("/")+1) : null;

      return roleService.convertToActualRoles(roles).collect().asList()
          .flatMap(list -> userProxy.assignRolesToUser(userId, new HashSet<>(list))) // assign user to realm roles
          .flatMap(list -> addUserToAppRole(userId, DEF_REGISTERED_CLIENT, newUserRoleGroup)) // assign user to specific client (app) roles
          .flatMap(response -> getUser(userId));
    });
  }

  // Register STUDENT via api
  @Override
  public Uni<RegisterRes> register(RegisterDto dto, Set<String> roles) {
    // check if email has not been claimed
    // if not claimed, send OTP to user's email
    //
    Uni<User> userUniByEmail = userRepository.findByEmail(dto.email().toLowerCase());
    return Panache.withTransaction(() -> {
      return userUniByEmail.flatMap(user -> {
        if(user == null) {
          // continue with the registration
          logger.info("user is null: {}", dto.email());
          return register(dto, roles, UserType.STUDENT, NEW_USER_ROLE_GRP) // register the student on OIDC
              .flatMap(this::createOtpAndSend); // create and send otp to the email when done
        }

        if(user.isClaimed()) {
          // end, user already exist
          return Uni.createFrom().failure(new AlreadyExistException("User with the email already exist"));
        }

        // initiate a claim process
        // send an otp to the email
        return createOtpAndSend(new RegisterRes(user.oidcId.toString(), user.name, user.email, user.username, user.enabled, user.type));
      });
    });
  }

  // IN-PROGRESS; NOT FULLY IMPLEMENTED
  @Override
  public Uni<RegisterRes> preRegister(PreRegisterDto dto, Set<String> roles) {
    // check if email has not been claimed
    // if not claimed, send OTP to user's email
    //
    Uni<User> userUniByEmail = userRepository.findByEmail(dto.email().toLowerCase());
    return Panache.withTransaction(() -> {
      return userUniByEmail.flatMap(user -> {
        if(user == null) {
          // continue with the registration
          logger.info("user is null: {}", dto.email());
          return save(dto, UserType.STUDENT) // register the student on OIDC
              .flatMap(this::createOtpAndSend); // create and send otp to the email when done
        }

        if(user.isClaimed()) {
          // end, user already exist
          return Uni.createFrom().failure(new AlreadyExistException("User with the email already exist"));
        }

        // initiate a claim process
        // send an otp to the email
        return createOtpAndSend(new RegisterRes(user.id.toString(), user.name, user.email, user.username, user.enabled, user.type));
      });
    });
  }

  public Uni<RegisterRes> save(PreRegisterDto register, UserType type) {
    logger.info("persist pre-register {} to database", register.name());
    User newUser = new User(register.name(), register.email(), type);
    return Panache.<User>withTransaction(newUser::persist)
        .map(user -> new RegisterRes(user.id.toString(), user.name, user.email, user.username, user.enabled, user.type));
  }

  private Uni<RegisterRes> createOtpAndSend(RegisterRes user) {
    logger.info("about to createOtpAndSend");
    return createOtp(user.email(), user)
        .flatMap(res -> mailerService.sendOtpMail(res.left, res.middle, res.right) // send the otp to the email
            .map(__ -> user));
  }

  private Uni<ImmutableTriple<RegisterRes, String, Long>> createOtp(String usernameOrEmail,  RegisterRes user) {
    logger.info("createOtp: {}, duration: {}", usernameOrEmail, otpKeyDuration);
    var otpKey = randomUtil.generateOtp();

    var responseUni = reactiveRedisClient.value(String.class).set(usernameOrEmail, otpKey)
        .flatMap(__ -> reactiveRedisClient.key(String.class).expire(usernameOrEmail, otpKeyDuration));

    logger.info("almost done with sendin otp: {}", otpKey);
    return responseUni
        .map(it -> ImmutableTriple.of(user, otpKey, otpKeyDuration));
  }

  @Override
  public Uni<AccountStatus> checkAccount(String emailOrUsername) {
    Uni<User> userUni = userRepository.findByUsernameOrEmail(emailOrUsername);

    return userUni.flatMap(user -> {
      if(user == null) {
        return Uni.createFrom().item(AccountStatus.REGISTER);
      }

      if(user.claimedDate != null) {
        return Uni.createFrom().item(AccountStatus.LOGIN);
      }

      return resendOtp(user.email)
          .flatMap(__ -> Uni.createFrom().item(AccountStatus.VERIFY));
    });
  }

  @Override
  public Uni<Void> verifyEmail(String email, String otpKey) {
    return validateOtp(email, otpKey)
        .flatMap(user -> enableVerifyEmail(user.oidcId.toString(), true, true) // enable user on OIDC
            .flatMap(__ -> {
              user.claimedDate = Instant.now();
              user.enabled = true;
              return Panache.<User>withTransaction(user::persist);
            })
            .flatMap(extUser -> { // send updated user-info to other service via the queuing system
              var res = new RegisterRes(extUser.oidcId.toString(), extUser.name, extUser.email, extUser.username, extUser.enabled, extUser.type);
              userRegister.send(res);
              return Uni.createFrom().item(extUser);
            })
        )
        .flatMap(user -> mailerService.sendWelcomeEmail(user));
  }

  @Override
  public Uni<Void> updateName(User user, UserBioUpdateDto dto) {
    Uni<Boolean> uniUsernameIsUsed = checkIfUsernameIsUsed.apply(user.id.toString(), dto.username());

    return uniUsernameIsUsed.flatMap(isUsed -> {
      var uni = (dto.username() == null || dto.username().isBlank())?
          userProxy.updateNameOnly(user.oidcId.toString(), new UpdateNameOnly(dto.name())) :
          userProxy.updateName(user.oidcId.toString(), new UpdateName(dto.name(), dto.username()));

      return uni
          .flatMap(__  -> {
            user.name = dto.name();
            user.username = dto.username() == null || dto.username().isBlank() ? user.username : dto.username();
            user.birthDate = dto.birthDate();
            user.idNumber = dto.idNumber() == null || dto.idNumber().isBlank() ? user.idNumber : dto.idNumber();
            return Panache.<User>withTransaction(user::persist);
          });
    }).flatMap(extUser -> Uni.createFrom().voidItem());
  }

  @Override
  public Uni<Void> updateUsername(User user, String username) {
    Uni<Boolean> uniUsernameIsUsed = checkIfUsernameIsUsed.apply(user.id.toString(), username);

    return uniUsernameIsUsed.flatMap(isUsed -> {
      return userProxy.updateUsername(user.oidcId.toString(), new UpdateUsername(username))
          .flatMap(__  -> {
            user.username = username;
            return Panache.<User>withTransaction(user::persist);
          });
    }).flatMap(extUser -> {
      var res = new RegisterRes(extUser.oidcId.toString(), extUser.name, extUser.email, extUser.username, extUser.enabled, extUser.type);
      userRegister.send(res);
      return Uni.createFrom().voidItem();
    });
  }

  @Override
  public Uni<Void> requestVerification(User user) {
    user.requestDate = Instant.now();
    return Panache.withTransaction(user::persist)
        .flatMap(__ -> Uni.createFrom().voidItem());
  }

  @Override
  public Uni<String> approveVerification(String username, User user) {
    user.verifiedDate = Instant.now();
    user.verifiedBy = username;
    return Panache.<User>withTransaction(user::persist)
        .flatMap(x -> Uni.createFrom().item(x.oidcId.toString()));
  }

  @Override
  public Uni<Void> resendOtp(String emailOrUsername) {
    Uni<User> userUniByEmail = userRepository.findByUsernameOrEmail(emailOrUsername.toLowerCase());

    return Panache.withTransaction(() -> {
      return userUniByEmail.flatMap(user -> {
        if(user == null) {
          // continue with the registration
          return Uni.createFrom().failure(new NotFoundException("User with the email not found"));
        }

        if(user.isClaimed()) {
          // end, user already exist
          return Uni.createFrom().failure(new AlreadyExistException("User with the email already exist"));
        }

        // initiate a claim process
        // send an otp to the email
        return createOtpAndSend(new RegisterRes(user.oidcId.toString(), user.name, user.email, user.username, user.enabled, user.type))
            .flatMap(__ -> Uni.createFrom().voidItem());
      });
    });
  }

  private Uni<User> validateOtp(String usernameOrEmail, String otpKey) {
    Uni<User> userUni = userRepository.findByUsernameOrEmail(usernameOrEmail)
        .onItem().ifNull().failWith(() -> new AuthenticationException("No user with this email/username found", AUTH_LOGIN_INVALID));

    return userUni.flatMap(user -> {
      return reactiveRedisClient.value(String.class).get(user.email).flatMap(response -> {
        if(response == null) {
          return Uni.createFrom().failure(() -> new AuthenticationException("Expired OTP", AUTH_OTP_EXPIRED));
        }

        if(!response.equalsIgnoreCase(otpKey)) {
          return Uni.createFrom().failure(() -> new AuthenticationException("Invalid OTP", AUTH_OTP_INVALID));
        }

        return reactiveRedisClient.key(String.class).del(usernameOrEmail)
            .flatMap(__ -> Uni.createFrom().item(user));
      });
    });
  }

  @Override
  @Deprecated
  public Uni<RegisterRes> registerCommunity(RegisterDto dto, Set<String> roles) {
    return register(dto, roles, UserType.COMMUNITY, NEW_USER_COM_ROLE_GRP);
  }

  @Override
  public Uni<RegisterRes> registerCommunity(String loggedInUserId, RegisterCommunityDto dto, Set<String> roles) {
    logger.debug("registerCommunity: {}, {}", loggedInUserId, dto);
    return findByOidcId(loggedInUserId).flatMap(user -> {
      logger.debug("registerCommunity: staff email: {}", user.email);
      var email = String.format("%s.%s", dto.username(), user.email);
      return register(email, dto, roles, UserType.COMMUNITY, NEW_USER_COM_ROLE_GRP);
    });
  }

  @Override
  public Uni<RegisterRes> registerClub(String loggedInUserId, RegisterCommunityDto dto, Set<String> roles) {
    logger.debug("registerClub: {}, {}", loggedInUserId, dto);
    return findByOidcId(loggedInUserId).flatMap(user -> {
      logger.debug("registerClub: community's owner/requester email: {}", user.email);
      var email = String.format("%s.%s", dto.username(), user.email);
      return register(email, dto, roles, UserType.CLUB, NEW_USER_CLB_ROLE_GRP);
    });
  }

  // Add an OIDC's user to client's role group
  private Uni<Void> addUserToAppRole(String userId, String clientId, String newUserRoleGroup) {
    logger.info("addUserToAppRole: userId: {}, appId: {}", userId, clientId);
    if(clientId == null) return Uni.createFrom().voidItem();

    return clientProxy.getByClientId(clientId).flatMap(apps -> {
      logger.info("addUserToAppRole: apps found {}", apps.size());
      if(apps.isEmpty()) return Uni.createFrom().voidItem();

      var app = apps.get(0); // get first app identified by the appId
      logger.info("addUserToAppRole: id: {}, name: {}", app.id, app.name);
      return clientProxy.getClientRoles(app.id, newUserRoleGroup.formatted(clientId.toUpperCase()))
          .onFailure().recoverWithNull()// if no role with the name found, return empty
          .flatMap(roles -> {
            logger.info("addUserToAppRole: roles: {}", roles);
            return assignClientRolesToUser(userId, app.id, roles);
          });
    }).onFailure().recoverWithNull();
  }

  public Uni<Void> assignClientRolesToUser(String userId, String appId, List<Role> roles) {
    logger.info("addUserToRoles: userId: {}, appId: {}: roles: {}", userId, appId, roles.size());
    if(roles.isEmpty()) return Uni.createFrom().voidItem();

    return userProxy.assignClientRolesToUser(userId, appId, new HashSet<>(roles))
        .flatMap(it -> Uni.createFrom().voidItem());
  }

  @Override
  public Uni<KUser> findOne(String userId) {
    return getUser(userId);
  }

  @Override
  public Uni<KUser> update(String userId, UserDto dto) {
    return getUserOnly(userId).flatMap(user -> {
      return userProxy.update(user.id, new Register(user.email, dto.name()))
          .flatMap(__ -> getUser(userId));
    });
  }

  @Override
  public Uni<KUser> disableOrEnable(String userId, Boolean value) {
    return getUserOnly(userId)
        .flatMap(user -> userProxy.disableOrEnable(user.id, new EnableUser(value))
        .flatMap(res -> getUserOnly(userId)));
  }

  public Uni<KUser> enableVerifyEmail(String userId, Boolean enabled, Boolean verifiedEmail) {
    return getUserOnly(userId)
        .flatMap(user -> userProxy.enableVerifiedEmail(user.id, new EnableVerifyEmail(enabled, verifiedEmail))
            .flatMap(res -> getUserOnly(userId)));
  }

  private Uni<KUser> getUserOnly(String userId) {
    return userProxy.getUser(userId);
  }

  private Uni<KUser> getUser(String userId) {
    return userProxy.getUser(userId).flatMap(user -> userProxy.getUserRoles(user.id).map(list -> {
      user.roles = list.stream().map(r -> r.name).collect(Collectors.toSet());
      return user;
    }));
  }

  @Override
  public Uni<User> save(RegisterRes register, UserType type) {
    logger.info("persisting {} to database", register.name());
    boolean isEmail = ValidatorUtil.checkIfValid(register.username());
    String username = isEmail? null : register.username();
    User user = new User(register.id(), register.name(), register.email(), username, type, register.enabled());
    return Panache.withTransaction(user::persist);
  }

  @Override
  public Uni<Void> upload(FileDto dto){
    return institutionService.findById(dto.institutionId)
        .flatMap(Unchecked.function(institution ->
            extract(Poiji.fromExcel(getStream(dto), getType(dto), ExcelFile.class), institution)));
  }

  private InputStream getStream(FileDto dto) throws IOException {
    return Files.newInputStream(dto.file.uploadedFile());
  }

  private PoijiExcelType getType(FileDto dto) {
    var extension = FileNameUtils.getExtension(dto.file.fileName());

    return PoijiExcelType.valueOf(extension.toUpperCase());
  }

  private Uni<Void> extractAndRegister(List<ExcelFile> contents, Institution institution) {
    Set<ExcelFile> uniqueContents = new HashSet<>(contents);

    Set<Uni<User>> unis = uniqueContents.stream().map(it -> {
      var uniRegister = registerOnOIDC(it.name, it.email, usernameDefaultPassword, it.type);

      return uniRegister
          .flatMap(kUser -> createUser(institution, it, kUser.id));
    }).collect(Collectors.toSet());

    return Uni.combine().all().unis(unis).discardItems();
  }

  private Uni<Void> extract(List<ExcelFile> contents, Institution institution) {
    Set<ExcelFile> uniqueContents = new HashSet<>(contents);

    Set<Uni<User>> unis = uniqueContents.stream()
        .map(it -> {
          return createUser(institution, it)
              .map(user -> {
                // send the persisted user to a queue for OIDC registration
                userOidcRegister.send(new RegisterUser(
                    user.oidcId.toString(),
                    new RegisterDto(it.name, it.email, usernameDefaultPassword), Set.of("USER"), it.type
                ));

                return user;
              });
        })
        .collect(Collectors.toSet());

    return Uni.combine().all().unis(unis).discardItems();
  }

  @Override
  public Uni<User> updateId(String currentId, KUser kUser) {
    logger.info("updateId: currentId: {}, kUserId: {}", currentId, kUser.id);
    return Panache.withTransaction(() -> {
      return userRepository.findByOidcId(currentId).flatMap(user -> {
        if(user == null) {
          return Uni.createFrom().nullItem();
        }

        user.oidcId = UUID.fromString(kUser.id);
        user.status = UserStatus.DONE;

        return Panache.withTransaction(user::persist);
      });
    });
  }

  public Uni<User> createUser(Institution institution, ExcelFile line) {
    Uni<Boolean> uniIdNumberIsUsed = checkIfIdNumberIsUsed.apply(null, line.idNumber);
    Uni<Boolean> uniEmailIsUsed = checkIfEmailIsUsed.apply(null, line.email);

    Uni<Course> courseUni = findOrCreateCourse(line.course, line.school, institution);

    return uniEmailIsUsed.flatMap(emailIsUsed -> uniIdNumberIsUsed)
        .flatMap(idIsUsed -> courseUni)
        .flatMap(course -> {
          var user = new User(line, course);
          return Panache.withTransaction(user::persist);
        });
  }

  public Uni<User> createUser(Institution institution, ExcelFile line, String id) {
    Uni<Course> courseUni = findOrCreateCourse(line.course, line.school, institution);

    return courseUni.flatMap(course -> {
      var user = new User(id, line, course);
      return Panache.withTransaction(user::persist);
    });
  }

  Function<CourseRes, Uni<Course>> findCourse = (CourseRes courseRes) -> {
    if(courseRes == null || courseRes.id() == null)
      return Uni.createFrom().nullItem();

    return courseService.findById(courseRes.id());
  };

  Function<SchoolRes, Uni<School>> findSchool = (SchoolRes schoolRes) -> {
    if(schoolRes == null || schoolRes.id() == null)
      return Uni.createFrom().nullItem();

    return schoolService.findById(schoolRes.id());
  };

  BiFunction<String, String, Uni<Boolean>> checkIfIdNumberIsUsed = (String id, String idNumber) -> {
    if(idNumber == null) return Uni.createFrom().failure(new Exception("IdNumber cannot be null"));

    Uni<Boolean> uni = id == null? checkIfIdNumberIsUsed(idNumber) : checkIfIdNumberIsUsedButNotId(id, idNumber);

    return uni.flatMap(inUsed -> {
      if(inUsed) {
        return Uni.createFrom().failure(() -> new AlreadyExistException(String.format("IdNumber: %s, already in used", idNumber)));
      }

      return Uni.createFrom().item(true);
    });
  };

  BiFunction<String, String, Uni<Boolean>> checkIfUsernameIsUsed = (String id, String username) -> {
    if(username == null || username.isBlank()) return Uni.createFrom().item(true);

    if(List.of(usernameBlacklist).contains(username))
      return Uni.createFrom().failure(new AlreadyExistException(String.format("Username: %s, already in used", username)));

    Uni<Boolean> uni = id == null? checkIfUsernameIsUsed(username) : checkIfUsernameIsUsedButNotId(id, username);

    return uni.flatMap(inUsed -> {
      if(inUsed) {
        return Uni.createFrom().failure(() -> new AlreadyExistException(String.format("Username: %s, already in used", username)));
      }

      return Uni.createFrom().item(true);
    });
  };

  BiFunction<String, String, Uni<Boolean>> checkIfEmailIsUsed = (String id, String email) -> {
    if(email == null) return Uni.createFrom().failure(new Exception("Email cannot be null"));

    Uni<Boolean> uni = id == null? checkIfEmailIsUsed(email) : checkIfEmailIsUsedButNotId(id, email);

    return uni.flatMap(inUsed -> {
      if(inUsed) {
        return Uni.createFrom().failure(() -> new AlreadyExistException(String.format("Email: %s, already in used", email)));
      }
      return Uni.createFrom().item(true);
    });
  };


  private Uni<KUser> registerOnOIDC(String name, String email, String password, UserType type) {
    return  registerOnOIDC(new RegisterDto(name, email, password), Set.of("USER"), type);
  }


  @Override
  public Uni<User> createStudent(UserReq req, UserType type) {
    Uni<Course> uniCourse = findCourse.apply(req.course());

    return Panache.withTransaction(() -> createUser(req, type))
        .flatMap(kUser -> uniCourse.flatMap(course -> {
          var newUser = new User(kUser.id, req, type, course);
          return Panache.withTransaction(newUser::persist);
        }));
  }

  @Override
  public Uni<User> createStaff(UserReq req, UserType type) {
    Uni<School> uniSchool = findSchool.apply(req.school());

    return Panache.withTransaction(() -> createUser(req, type))
        .flatMap(kUser -> uniSchool.flatMap(school -> {
          var newUser = new User(kUser.id, req, type, school);
          return Panache.withTransaction(newUser::persist);
        }));
  }

  public Uni<KUser> createUser(UserReq req, UserType type) {
    Uni<Boolean> uniIdNumberIsUsed = checkIfIdNumberIsUsed.apply(null, req.idNumber());
    Uni<Boolean> uniEmailIsUsed = checkIfEmailIsUsed.apply(null, req.idNumber());

    return Panache.withTransaction(() ->
        uniEmailIsUsed
            .flatMap(email -> uniIdNumberIsUsed)
            .flatMap(numberIsUsed -> registerOnOIDC(req.name(), req.email(), usernameDefaultPassword, type)) // register user on OIDC
    );
  }

  private Uni<User> updateUser(String id, UserReq req) {
    Uni<User> uniExtUser = findById(id);
    Uni<Boolean> uniIdNumberIsUsed = checkIfIdNumberIsUsed.apply(id, req.idNumber());
    Uni<Boolean> uniEmailIsUsed = checkIfEmailIsUsed.apply(id, req.email());

    return uniExtUser.flatMap(extuser -> {
      return uniEmailIsUsed
          .flatMap(emailIsUsed -> uniIdNumberIsUsed)
          .flatMap(numberIsUsed -> update(extuser.oidcId.toString(),  new UserDto(req.name())))
          .flatMap(kUser -> Uni.createFrom().item(extuser));
    });
  }

  @Override
  public Uni<User> updateStudent(String id, UserReq req) {
    Uni<Course> uniCourse = findCourse.apply(req.course());

    return Panache.withTransaction(() ->
        uniCourse.flatMap(course ->
            updateUser(id, req).flatMap(extUser -> {
              extUser.name = req.name();
              extUser.birthDate = req.birthDate();
              extUser.idNumber = req.idNumber();
              extUser.course = course;
              return Panache.withTransaction(extUser::persist);
            })
        )
    );
  }

  @Override
  public Uni<User> updateStaff(String id, UserReq req) {
    Uni<School> uniSchool = findSchool.apply(req.school());

    return Panache.withTransaction(() ->
        uniSchool.flatMap(school ->
            updateUser(id, req).flatMap(extUser -> {
              logger.info("school-info: name: {}", school.name);
              extUser.name = req.name();
              extUser.birthDate = req.birthDate();
              extUser.idNumber = req.idNumber();
              extUser.school = school;
              return Panache.withTransaction(extUser::persist);
            })
        )
    );
  }

  @Override
  public Uni<User> disableOrEnableUser(String id, Boolean value) {
    Uni<User> userUni = findById(id);

    return Panache.withTransaction(() ->
        userUni.flatMap(extUser -> {
          if(extUser.enabled && value) {
            return Uni.createFrom().failure(() -> new BadRequestException("This is user is already enabled"));
          }

          if(!extUser.enabled && !value) {
            return Uni.createFrom().failure(() -> new BadRequestException("This is user is already disabled"));
          }

          return disableOrEnable(extUser.oidcId.toString(), value).flatMap(kUser -> {
            extUser.enabled = kUser.enabled;
            return Panache.withTransaction(extUser::persist);
          });
        })
    );
  }

  @Override
  public Uni<Void> delete(String id) {
    return Panache.withTransaction(() ->
        findById(id).flatMap(PanacheEntityBase::delete)
    );
  }

  @Override
  public Uni<User> update(User extUser, UserUpdateDto dto) {
    return null;
  }

  @Override
  public Uni<User> updateAvatar(String id, PhotoDto dto) {
    Uni<User> userUni = findByOidcId(id);

    return Panache.withTransaction(() ->
        userUni.flatMap(user -> {
          logger.info("user found: {}", user);
          Uni<CloudinaryRes> uploadImage = dto.file() != null?
              imageUploader(dto.file()) : Uni.createFrom().nullItem();

          return uploadImage.flatMap(imgurResponse -> {
            logger.info("return: {}", imgurResponse);
            if(imgurResponse != null) {
              user.avatarUrl = imgurResponse.secureUrl();
              user.avatarType = imgurResponse.getType();

              // TODO:: TO BE REMOVED
//              UserUpdatedRes res = new UserUpdatedRes();
//              res.id = user.oidcId.toString();
//              res.imageType = imgurResponse.data.type();
//              res.imageUrl = imgurResponse.data.link();
//              userUpdatedResEmitter.send(res);
            }
            return Panache.withTransaction(user::persist);
          });
        })
    );
  }

  public Uni<CloudinaryRes> imageUploader(File image) {
    return cloudinaryProxy.sendMultipartData(image, cloudinaryKey, cloudinaryPreset);
  }


  @CacheResult(cacheName = "course-cache")
  public Uni<Course> findOrCreateCourse(
      @CacheKey String courseName, @CacheKey String schoolName, @CacheKey Institution institution
  ) {
    Uni<School> schoolUni = schoolService.findOrCreate(schoolName, institution);

    return schoolUni.flatMap(school -> courseService.findOrCreate(courseName, school))
        .memoize().indefinitely();
  }

  @CacheResult(cacheName = "course-cache")
  public Uni<Course> findOrCreateCourse(
      @CacheKey Tuple2<String, String> courseSchool,
      @CacheKey Institution institution
  ) {
    String courseName = courseSchool.getItem1();
    String schoolName = courseSchool.getItem2();

    Uni<School> schoolUni = schoolService.findOrCreate(schoolName, institution);

    return schoolUni.flatMap(school -> courseService.findOrCreate(courseName, school))
        .memoize().indefinitely();
  }

  public Uni<Boolean> suspendUser(String oidcId) {
    Uni<User> userUni = findByOidcId(oidcId);

    return userUni.flatMap(extUser -> {
      return disableOrEnable(extUser.oidcId.toString(), false).flatMap(kUser -> {
        extUser.enabled = kUser.enabled;
        extUser.suspendedDate = Instant.now();
        return Panache.withTransaction(extUser::persist)
            .map(updatedUser -> true);
      });
    });
  }

//  private RegisterRes createNew(String id, User user) {
//   return new RegisterRes(user.id, user.firstName, user.lastName, user.email, user.username, false, type)
//  }
}
