package io.github.jelilio.smbackend.usermanager.base;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Map;

public class KeycloakResource implements QuarkusTestResourceLifecycleManager {
  KeycloakContainer keycloak;

  @Override
  public  void init(Map<String, String> initArgs) {
    initArgs.forEach((k, v) -> System.out.println("keycloak image " + k + "=" + v));
  }

  @Override
  public Map<String, String> start() {
    keycloak = new KeycloakContainer()
        .withRealmImportFile("/quarkus-realm.json");
    keycloak.start();

    return Map.of(
        "quarkus.oidc.auth-server-url", keycloak.getAuthServerUrl() + "/realms/connect",
        "quarkus.oidc.credentials.secret", "60mHDB5qYy3JmVayMqAMxKi2SOrOK3et"
    );
  }

  @Override
  public void stop() {
    if (keycloak != null) {
      keycloak.stop();
    }
  }
}
