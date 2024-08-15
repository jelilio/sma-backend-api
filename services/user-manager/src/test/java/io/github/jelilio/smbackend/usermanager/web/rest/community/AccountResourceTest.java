package io.github.jelilio.smbackend.usermanager.web.rest.community;

import io.github.jelilio.smbackend.common.dto.RegisterCommunityDto;
import org.hamcrest.core.Is;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.HttpHeaders.ACCEPT;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.Response.Status.CREATED;
import static org.junit.jupiter.api.Assertions.assertNotNull;

//@QuarkusTest
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@QuarkusTestResource(MockAuthorizationServerTestResource.class)
public class AccountResourceTest  {
  private static final String JSON = "application/json;charset=UTF-8";
  private static final String TEXT = "text/plain";
  private static final String DEFAULT_NAME = "Test Club";
  private static final String DEFAULT_CLUB_USERNAME = "TestClub";
  private static final String DEFAULT_CLUB_BIO = "Test bio";
  private static final String DEFAULT_NAME2 = "Test Institution2";
  private static final String DEFAULT_UPDATED_NAME = "Test Institution - Updated";
  private static final String DEFAULT_PASSWORD = "password";
  private static final String BEARER_TOKEN = "337aab0f-b547-489b-9dbd-a54dc7bdf20d";

//  @Test
//  @Order(1)
//  @TestSecurity(
//      user = "testUser", roles = {"newsfeed.ROLE_USER_COM"},
//      attributes={@SecurityAttribute(key = "sub", value = "test-id")}
//  )
//  @OidcSecurity(claims = {
//      @Claim(key = "email", value = "user@gmail.com")
//  }, userinfo = {
//      @UserInfo(key = "sub", value = "test-id")
//  }, config = {
//      @ConfigMetadata(key = "issuer", value = "issuer")
//  })
  void canRegisterClub() {
    var validateOtpDto = new RegisterCommunityDto(DEFAULT_NAME, DEFAULT_CLUB_USERNAME, DEFAULT_CLUB_BIO, DEFAULT_PASSWORD);

    var id =  given()
        .body(validateOtpDto)
        .header(CONTENT_TYPE, JSON)
        .header(ACCEPT, JSON)
        .when()
        .post("/api/community/account/register-club")
        .then()
        .statusCode(CREATED.getStatusCode())
        .header(CONTENT_TYPE, JSON)
        .body("name", Is.is(DEFAULT_NAME))
        .extract().body().jsonPath().getString("id");

    assertNotNull(id);
  }

}
