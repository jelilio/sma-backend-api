package io.github.jelilio.smbackend.usermanager.service;

import io.github.jelilio.smbackend.common.dto.SchoolReq;
import io.github.jelilio.smbackend.common.dto.response.InstitutionRes;
import io.github.jelilio.smbackend.common.exception.NotFoundException;
import io.github.jelilio.smbackend.usermanager.entity.Institution;
import io.github.jelilio.smbackend.usermanager.entity.School;
import io.github.jelilio.smbackend.usermanager.repository.InstitutionRepository;
import io.github.jelilio.smbackend.usermanager.repository.SchoolRepository;
import io.github.jelilio.smbackend.usermanager.service.impl.SchoolServiceImpl;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;


@QuarkusTest
public class SchoolServiceTest {
  private static final String DEFAULT_NAME = "Test School";
  private static final String DEFAULT_DESCRIPTION = "Test School Description";

  private static final String DEFAULT_UPDATED_NAME = "Updated School";
  private static final String DEFAULT_UPDATED_DESCRIPTION = "Updated School Description";


  @Inject
  SchoolServiceImpl service;

  @InjectMock
  SchoolRepository schoolRepository;

  @InjectMock
  InstitutionRepository institutionRepository;


  @Test
  @RunOnVertxContext
  public void canFindByIdToReturnNull(UniAsserter asserter) {
    var id = UUID.randomUUID().toString();

    asserter.execute(() -> Mockito.when(schoolRepository.findById(id))
        .thenReturn(Uni.createFrom().nullItem()));

    asserter.assertFailedWith(() -> {
      try {
        return service.findById(id);
      } catch (Exception e) {
        return Uni.createFrom().failure(e);
      }
    }, t -> assertEquals(NotFoundException.class, t.getClass()));

    asserter.surroundWith(u -> Panache.withSession(() -> u));
  }

  @Test
  @RunOnVertxContext
  public void canFindByIdToReturnObject(UniAsserter asserter) {
    var id = UUID.randomUUID();

    var school = new School();
    school.id = id;
    school.name = DEFAULT_NAME;
    school.description = DEFAULT_DESCRIPTION;

    asserter.execute(() -> Mockito.when(schoolRepository.findById(id.toString())).thenReturn(Uni.createFrom().item(school)));
    asserter.assertNotNull(() -> service.findById(id.toString()));

    asserter.surroundWith(u -> Panache.withSession(() -> u));
  }

  @Test
  @RunOnVertxContext
  public void canUpdateExistingSchool(UniAsserter asserter) {
    var id = UUID.randomUUID();
    var institutionId = UUID.randomUUID();

    var institutionRes = new InstitutionRes(institutionId.toString(), "Institution", "Institution Description");

    var extInstitution = new Institution();
    extInstitution.id = institutionId;
    extInstitution.name = "Institution";
    extInstitution.description = "Institution Description";

    var extSchool = new School();
    extSchool.id = id;
    extSchool.name = DEFAULT_NAME;
    extSchool.description = DEFAULT_DESCRIPTION;

    var updatedSchool = new School();
    updatedSchool.id = id;
    updatedSchool.name = DEFAULT_UPDATED_NAME;
    updatedSchool.description = DEFAULT_UPDATED_DESCRIPTION;

    var req = new SchoolReq(DEFAULT_UPDATED_NAME, DEFAULT_UPDATED_DESCRIPTION, institutionRes);

    System.out.println("id: " + id);
    System.out.println("extSchool.id: " + extSchool.id);
    System.out.println("updatedSchool.id: " + updatedSchool.id);

    asserter.execute(() -> {
      Mockito.when(institutionRepository.findById(institutionId.toString())).thenReturn(Uni.createFrom().item(extInstitution));
      Mockito.when(schoolRepository.findById(id.toString())).thenReturn(Uni.createFrom().item(extSchool));
      Mockito.when(schoolRepository.countByNameAndInstitutionButNotId(id.toString(), DEFAULT_NAME, extInstitution)).thenReturn(Uni.createFrom().item(0L));
      Mockito.when(schoolRepository.persist(extSchool)).thenReturn(Uni.createFrom().item(updatedSchool));
    });

    asserter.assertNotNull(() -> service.update(id.toString(), req));

    asserter.surroundWith(u -> Panache.withSession(() -> u));
  }
}
