package io.github.jelilio.smbackend.usermanager.web.rest.admin;

import io.github.jelilio.smbackend.common.dto.CourseReq;
import io.github.jelilio.smbackend.common.dto.InstitutionReq;
import io.github.jelilio.smbackend.common.dto.SchoolReq;
import io.github.jelilio.smbackend.common.dto.response.InstitutionRes;
import io.github.jelilio.smbackend.common.dto.response.SchoolRes;
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
@Order(3)
@QuarkusTestResource(KeycloakResource.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CoursesResourceTest extends AccessTokenProvider {
  private static final String JSON = "application/json;charset=UTF-8";
  private static final String TEXT = "text/plain";
  private static final String DEFAULT_NAME = "Test Course";
  private static final String DEFAULT_UPDATED_NAME = "Test Course - Updated";
  private static final String DEFAULT_DESCRIPTION = "Test Course Description";

  private static final String DEFAULT_SCHOOL_NAME = "Test School";
  private static final String DEFAULT_SCHOOL_DESCRIPTION = "Test School Description";

  private static final String DEFAULT_INSTITUTION_NAME = "Test Institution";
  private static final String DEFAULT_INSTITUTION_DESCRIPTION = "Test Institution Description";

  private static String id;
  private static String institutionId;
  private static String schoolId;

  /* */

  @Test
  @Order(1)
  void canFetchAll() {
    given()
        .auth()
        .oauth2(getAdminAccessToken())
        .when().get("/api/admin/courses/all")
        .then()
        .statusCode(OK.getStatusCode());
  }

  @Test
  @Order(2)
  void canCreateInstitutionAndSchool() {
    var validateOtpDto = new InstitutionReq(DEFAULT_INSTITUTION_NAME, DEFAULT_INSTITUTION_DESCRIPTION);

    institutionId =  given()
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
        .body("name", Is.is(DEFAULT_INSTITUTION_NAME))
        .body("description", Is.is(DEFAULT_INSTITUTION_DESCRIPTION))
        .extract().body().jsonPath().getString("id");

    assertNotNull(institutionId);

    var institutionRes = new InstitutionRes(institutionId, DEFAULT_INSTITUTION_NAME, DEFAULT_INSTITUTION_DESCRIPTION);
    var schoolReq = new SchoolReq(DEFAULT_SCHOOL_NAME, DEFAULT_SCHOOL_DESCRIPTION, institutionRes);

    schoolId =  given()
        .auth()
        .oauth2(getAdminAccessToken())
        .body(schoolReq)
        .header(CONTENT_TYPE, JSON)
        .header(ACCEPT, JSON)
        .when()
        .post("/api/admin/schools")
        .then()
        .statusCode(CREATED.getStatusCode())
        .header(CONTENT_TYPE, JSON)
        .body("name", Is.is(DEFAULT_SCHOOL_NAME))
        .body("description", Is.is(DEFAULT_SCHOOL_DESCRIPTION))
        .extract().body().jsonPath().getString("id");

    assertNotNull(schoolId);
  }

  @Test
  @Order(3)
  void canCreateCourse() {
    var institutionRes = new InstitutionRes(institutionId, DEFAULT_INSTITUTION_NAME, DEFAULT_INSTITUTION_DESCRIPTION);
    var schoolRes = new SchoolRes(schoolId, DEFAULT_SCHOOL_NAME, institutionRes, DEFAULT_SCHOOL_DESCRIPTION);
    var validateOtpDto = new CourseReq(DEFAULT_NAME, DEFAULT_DESCRIPTION, schoolRes);

    id =  given()
        .auth()
        .oauth2(getAdminAccessToken())
        .body(validateOtpDto)
        .header(CONTENT_TYPE, JSON)
        .header(ACCEPT, JSON)
        .when()
        .post("/api/admin/courses")
        .then()
        .statusCode(CREATED.getStatusCode())
        .header(CONTENT_TYPE, JSON)
        .body("name", Is.is(DEFAULT_NAME))
        .body("description", Is.is(DEFAULT_DESCRIPTION))
        .extract().body().jsonPath().getString("id");

    assertNotNull(id);
  }

  @Test
  @Order(4)
  void canUpdateCourse() {
    var institutionRes = new InstitutionRes(institutionId, DEFAULT_INSTITUTION_NAME, DEFAULT_DESCRIPTION);
    var schoolRes = new SchoolRes(schoolId, DEFAULT_SCHOOL_NAME, institutionRes, DEFAULT_SCHOOL_DESCRIPTION);
    var updatedDto = new CourseReq(DEFAULT_UPDATED_NAME, DEFAULT_DESCRIPTION, schoolRes);

    given()
        .auth()
        .oauth2(getAdminAccessToken())
        .body(updatedDto)
        .header(CONTENT_TYPE, JSON)
        .header(ACCEPT, JSON)
        .when()
        .put(String.format("/api/admin/courses/%s", id))
        .then()
        .statusCode(OK.getStatusCode())
        .header(CONTENT_TYPE, JSON)
        .body("id", Is.is(id))
        .body("name", Is.is(DEFAULT_UPDATED_NAME))
        .body("description", Is.is(DEFAULT_DESCRIPTION))
        .extract().body().jsonPath().getString("id");
  }

  @Test
  @Order(5)
  void canFindCourseById() {
    given()
        .auth()
        .oauth2(getAdminAccessToken())
        .header(CONTENT_TYPE, JSON)
        .header(ACCEPT, JSON)
        .when()
        .get(String.format("/api/admin/courses/%s", id))
        .then()
        .statusCode(OK.getStatusCode())
        .header(CONTENT_TYPE, JSON)
        .body("id", Is.is(id))
        .body("name", Is.is(DEFAULT_UPDATED_NAME))
        .body("description", Is.is(DEFAULT_DESCRIPTION));
  }

  @Test
  @Order(6)
  void canDeleteCourseById() {
    given()
        .auth()
        .oauth2(getAdminAccessToken())
        .header(CONTENT_TYPE, JSON)
        .header(ACCEPT, JSON)
        .when()
        .delete(String.format("/api/admin/courses/%s", id))
        .then()
        .statusCode(OK.getStatusCode());
  }
  
  @Test
  @Order(7)
  void canNotFoundAfterDelete() {
    given()
        .auth()
        .oauth2(getAdminAccessToken())
        .header(CONTENT_TYPE, JSON)
        .header(ACCEPT, JSON)
        .when()
        .get(String.format("/api/admin/courses/%s", id))
        .then()
        .statusCode(NOT_FOUND.getStatusCode());
  }

  @Test
  @Order(8)
  void shouldCleanUp() {
    given()
        .auth()
        .oauth2(getAdminAccessToken())
        .header(CONTENT_TYPE, JSON)
        .header(ACCEPT, JSON)
        .when()
        .delete(String.format("/api/admin/schools/%s", schoolId))
        .then()
        .statusCode(OK.getStatusCode());

    given()
        .auth()
        .oauth2(getAdminAccessToken())
        .header(CONTENT_TYPE, JSON)
        .header(ACCEPT, JSON)
        .when()
        .delete(String.format("/api/admin/institutions/%s", institutionId))
        .then()
        .statusCode(OK.getStatusCode());
  }
  /**/
}
