package io.github.jelilio.smbackend.usermanager.web.rest.admin;

import io.github.jelilio.smbackend.common.dto.InstitutionReq;
import io.github.jelilio.smbackend.usermanager.base.AccessTokenProvider;
import io.github.jelilio.smbackend.usermanager.base.KeycloakResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.HttpHeaders.ACCEPT;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@Order(1)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(KeycloakResource.class)
public class InstitutionsResourceTest extends AccessTokenProvider {
  private static final String JSON = "application/json;charset=UTF-8";
  private static final String TEXT = "text/plain";
  private static final String DEFAULT_NAME = "Test Institution";
  private static final String DEFAULT_NAME2 = "Test Institution2";
  private static final String DEFAULT_UPDATED_NAME = "Test Institution - Updated";
  private static final String DEFAULT_DESCRIPTION = "Test Institution Description";

  private static String id;

  @Test
  @Order(1)
  void canFetchAll() {
    given()
        .auth()
        .oauth2(getAdminAccessToken())
        .when().get("/api/admin/institutions/all")
        .then()
        .statusCode(OK.getStatusCode());
  }

  @Test
  @Order(2)
  void canCreateInstitution() {
    var validateOtpDto = new InstitutionReq(DEFAULT_NAME, DEFAULT_DESCRIPTION);

    id =  given()
        .auth()
        .oauth2(getAdminAccessToken())
        .body(validateOtpDto)
        .header(CONTENT_TYPE, JSON)
        .header(ACCEPT, JSON)
        .when()
        .post("/api/admin/institutions")
        .then()
        .statusCode(CREATED.getStatusCode())
        .header(CONTENT_TYPE, JSON)
        .body("name", Is.is(DEFAULT_NAME))
        .body("description", Is.is(DEFAULT_DESCRIPTION))
        .extract().body().jsonPath().getString("id");

    assertNotNull(id);
  }

  @Test
  @Order(3)
  void canUpdateInstitution() {
    var updatedDto = new InstitutionReq(DEFAULT_UPDATED_NAME, DEFAULT_DESCRIPTION);

    given()
        .auth()
        .oauth2(getAdminAccessToken())
        .body(updatedDto)
        .header(CONTENT_TYPE, JSON)
        .header(ACCEPT, JSON)
        .when()
        .put(String.format("/api/admin/institutions/%s", id))
        .then()
        .statusCode(OK.getStatusCode())
        .header(CONTENT_TYPE, JSON)
        .body("id", Is.is(id))
        .body("name", Is.is(DEFAULT_UPDATED_NAME))
        .body("description", Is.is(DEFAULT_DESCRIPTION))
        .extract().body().jsonPath().getString("id");
  }

  @Test
  @Order(4)
  void canFindById() {
    given()
        .auth()
        .oauth2(getAdminAccessToken())
        .header(CONTENT_TYPE, JSON)
        .header(ACCEPT, JSON)
        .when()
        .get(String.format("/api/admin/institutions/%s", id))
        .then()
        .statusCode(OK.getStatusCode())
        .header(CONTENT_TYPE, JSON)
        .body("id", Is.is(id))
        .body("name", Is.is(DEFAULT_UPDATED_NAME))
        .body("description", Is.is(DEFAULT_DESCRIPTION));
  }

  @Test
  @Order(5)
  void canDeleteById() {
    given()
        .auth()
        .oauth2(getAdminAccessToken())
        .header(CONTENT_TYPE, JSON)
        .header(ACCEPT, JSON)
        .when()
        .delete(String.format("/api/admin/institutions/%s", id))
        .then()
        .statusCode(OK.getStatusCode());
  }

  @Test
  @Order(6)
  void canNotFoundAfterDelete() {
    given()
        .auth()
        .oauth2(getAdminAccessToken())
        .header(CONTENT_TYPE, JSON)
        .header(ACCEPT, JSON)
        .when()
        .get(String.format("/api/admin/institutions/%s", id))
        .then()
        .statusCode(NOT_FOUND.getStatusCode());
  }
}
