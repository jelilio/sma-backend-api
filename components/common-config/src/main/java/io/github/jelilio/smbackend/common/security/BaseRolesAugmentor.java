package io.github.jelilio.smbackend.common.security;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import io.smallrye.mutiny.Uni;
import jakarta.json.JsonString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

public class BaseRolesAugmentor implements SecurityIdentityAugmentor {
  private static final Logger logger = LoggerFactory.getLogger(BaseRolesAugmentor.class);

  private final static String ROLES = "roles";
  private final static String REALM_ACCESS = "realm_access";
  private final static String RESOURCE_ACCESS = "resource_access";

  @Override
  public int priority() {
    return 0;
  }

  @Override
  public Uni<SecurityIdentity> augment(SecurityIdentity securityIdentity, AuthenticationRequestContext authenticationRequestContext) {
    Uni<SecurityIdentity> cs = Uni.createFrom().nullItem();
    if (securityIdentity.isAnonymous()) {
      return Uni.createFrom().item(securityIdentity);
    } else {
      // create a new builder and copy principal, attributes, credentials and roles from the original
      DefaultJWTCallerPrincipal principal = (DefaultJWTCallerPrincipal) securityIdentity.getPrincipal();

      var realmAuthorities = extractRealmAuthorities(principal);
      var appAuthorities = extractClientAuthorities(principal);

      realmAuthorities.addAll(appAuthorities);

      logger.info("all roles: {}", realmAuthorities);
      System.out.println("all roles" + Arrays.toString(realmAuthorities.toArray()));

      QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder()
          .setPrincipal(securityIdentity.getPrincipal())
          .addAttributes(securityIdentity.getAttributes())
          .addCredentials(securityIdentity.getCredentials())
          .addRoles(realmAuthorities);

      return Uni.createFrom().item(builder.build());
    }
  }

  private Set<String> extractClientAuthorities(DefaultJWTCallerPrincipal principal) {
    var resourceAccess = (Map<String, Object>) Optional.ofNullable(principal.getClaim(RESOURCE_ACCESS)).get();

    if(resourceAccess == null) return emptySet();

    Set<Set<String>> sets = resourceAccess.entrySet().stream().map(it -> {
      var clientId = it.getKey();
      var appAccess = (Map<String, Object>) it.getValue();

      if(appAccess == null) return Set.<String>of();

      return ((List<?>)appAccess.getOrDefault(ROLES, List.of()))
          .stream()
          .map(res -> ((JsonString)res).getString())
          .map(role -> "%s.ROLE_%s".formatted(clientId, role))
          .map(String::new).collect(Collectors.toSet());
    }).collect(Collectors.toSet());

    return sets.stream()
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  private Set<String> extractRealmAuthorities(DefaultJWTCallerPrincipal principal) {
    var realmAccess = (Map<String, Object>) Optional.ofNullable(principal.getClaim(REALM_ACCESS)).get();

    return ((List<?>)realmAccess.getOrDefault(ROLES, List.of()))
        .stream()
        .map(it -> ((JsonString)it).getString())
        .map("ROLE_%s"::formatted)
        .map(String::new).collect(Collectors.toSet());
  }
}
