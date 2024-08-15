package io.github.jelilio.smbackend.usermanager.service;

import io.github.jelilio.smbackend.common.dto.*;
import io.github.jelilio.smbackend.common.dto.response.RegisterRes;
import io.github.jelilio.smbackend.common.entity.enumeration.AccountStatus;
import io.github.jelilio.smbackend.common.entity.enumeration.DateQueryType;
import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.usermanager.dto.UserDto;
import io.github.jelilio.smbackend.usermanager.entity.User;
import io.github.jelilio.smbackend.usermanager.model.KUser;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Set;

public interface UserService {
  Uni<User> findById(String id);

  Uni<User> findByOidcId(String id);

  Uni<List<User>> findAll();

  Uni<List<User>> findAllByQueryType(DateQueryType queryType);

  Uni<Paged<User>> findAll(int size, int index);

  Uni<List<User>> findAllByType(UserType type);

  Uni<List<User>> findAllByTypeAndQueryType(UserType type, DateQueryType queryType);

  Uni<Paged<User>> findAllByType(UserType type, int size, int index);

  Uni<KUser> registerOnOIDC(RegisterDto dto, Set<String> roles, UserType type);

  // Register user via api
  Uni<AccountStatus> checkAccount(String emailOrUsername);

  Uni<RegisterRes> register(RegisterDto dto, Set<String> roles);

  Uni<RegisterRes> preRegister(PreRegisterDto dto, Set<String> roles);

  Uni<Void> verifyEmail(String email, String otpKey);

  Uni<Void> updateName(User user, UserBioUpdateDto dto);

  Uni<Void> updateUsername(User user, String username);

  Uni<Void> resendOtp(String emailOrUsername);

  @Deprecated
  Uni<RegisterRes> registerCommunity(RegisterDto dto, Set<String> roles);

  Uni<RegisterRes> registerCommunity(String loggedInUserId, RegisterCommunityDto dto, Set<String> roles);

  Uni<RegisterRes> registerClub(String loggedInUserId, RegisterCommunityDto dto, Set<String> roles);

  Uni<KUser> findOne(String userId);

  Uni<KUser> update(String userId, UserDto dto);

  Uni<KUser> disableOrEnable(String userId, Boolean value);

  Uni<User> save(RegisterRes register, UserType type);

  Uni<Void> upload(FileDto dto);

  Uni<User> updateId(String currentId, KUser kUser);

  Uni<User> createStudent(UserReq userReg, UserType type);

  Uni<User> createStaff(UserReq req, UserType type);

  Uni<User> updateStudent(String id, UserReq req);

  Uni<User> updateStaff(String id, UserReq req);

  Uni<User> disableOrEnableUser(String id, Boolean value);

  Uni<Void> delete(String id);

  Uni<User> update(User extUser, UserUpdateDto dto);

  Uni<User> updateAvatar(String id, PhotoDto dto);

  Uni<Void> requestVerification(User user);

  Uni<String> approveVerification(String username, User user);

  Uni<Boolean> suspendUser(String oidcId);
}
