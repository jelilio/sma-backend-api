package io.github.jelilio.smbackend.usermanager.service;

import io.github.jelilio.smbackend.common.dto.InstitutionReq;
import io.github.jelilio.smbackend.common.exception.NotFoundException;
import io.github.jelilio.smbackend.usermanager.entity.Institution;
import io.github.jelilio.smbackend.usermanager.repository.InstitutionRepository;
import io.github.jelilio.smbackend.usermanager.service.impl.InstitutionServiceImpl;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;


@QuarkusTest
public class InstitutionServiceTest {
  private static final String DEFAULT_NAME = "Test institution";
  private static final String DEFAULT_DESCRIPTION = "Test Institution Description";

  private static final String DEFAULT_UPDATED_NAME = "Updated institution";
  private static final String DEFAULT_UPDATED_DESCRIPTION = "Updated Institution Description";


  @Inject
  InstitutionServiceImpl service;

  @InjectMock
  InstitutionRepository institutionRepository;

  @Test
  @RunOnVertxContext
  public void testEntity(UniAsserter asserter) {
    asserter.assertEquals(() -> institutionRepository.count(), 0L);

    asserter.execute(() -> Mockito.when(institutionRepository.findOrdered())
        .thenReturn(Uni.createFrom().item(Collections.emptyList())));
    asserter.assertThat(() -> institutionRepository.findOrdered(), List::isEmpty);

    asserter.surroundWith(u -> Panache.withSession(() -> u));
  }

  @Test
  @RunOnVertxContext
  public void canFindByIdToReturnNull(UniAsserter asserter) {
    var id = UUID.randomUUID().toString();

    asserter.execute(() -> Mockito.when(institutionRepository.findById(id))
        .thenReturn(Uni.createFrom().nullItem()));

//    asserter.assertNull(() -> institutionRepository.findById(id));
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

    var institution = new Institution();
    institution.id = id;
    institution.name = DEFAULT_NAME;
    institution.description = DEFAULT_DESCRIPTION;

    asserter.execute(() -> Mockito.when(institutionRepository.findById(id.toString()))
        .thenReturn(Uni.createFrom().item(institution)));

//    asserter.assertNotNull(() -> institutionRepository.findById(id.toString()));
    asserter.assertNotNull(() -> service.findById(id.toString()));

    asserter.surroundWith(u -> Panache.withSession(() -> u));
  }

  @Test
  @RunOnVertxContext
  public void canCreateNewInstitution(UniAsserter asserter) {
    var id = UUID.randomUUID();

    var req = new InstitutionReq(DEFAULT_NAME, DEFAULT_DESCRIPTION);

    var newly = new Institution();
    newly.name = DEFAULT_NAME;
    newly.description = DEFAULT_DESCRIPTION;

    var institution = new Institution();
    institution.id = id;
    institution.name = DEFAULT_NAME;
    institution.description = DEFAULT_DESCRIPTION;

    asserter.execute(() -> Mockito.when(institutionRepository.countByName(DEFAULT_NAME))
        .thenReturn(Uni.createFrom().item(0L)));
    asserter.execute(() -> Mockito.when(institutionRepository.persist(newly))
        .thenReturn(Uni.createFrom().item(institution)));

    asserter.assertNotNull(() -> service.create(req));

    asserter.surroundWith(u -> Panache.withSession(() -> u));
  }

  @Test
  @RunOnVertxContext
  public void canUpdateExistingInstitution(UniAsserter asserter) {
    var id = UUID.randomUUID();

    var extInstitution = new Institution();
    extInstitution.id = id;
    extInstitution.name = DEFAULT_NAME;
    extInstitution.description = DEFAULT_DESCRIPTION;

    var updatedInstitution = new Institution();
    updatedInstitution.id = id;
    updatedInstitution.name = DEFAULT_UPDATED_NAME;
    updatedInstitution.description = DEFAULT_UPDATED_DESCRIPTION;

    var req = new InstitutionReq(DEFAULT_UPDATED_NAME, DEFAULT_UPDATED_DESCRIPTION);

    System.out.println("id: " + id);
    System.out.println("extInstitution.id: " + extInstitution.id);
    System.out.println("updatedInstitution.id: " + updatedInstitution.id);

    asserter.execute(() -> {
      Mockito.when(institutionRepository.findById(id.toString())).thenReturn(Uni.createFrom().item(extInstitution));
      Mockito.when(institutionRepository.countByNameButNotId(id.toString(), DEFAULT_NAME)).thenReturn(Uni.createFrom().item(0L));
      Mockito.when(institutionRepository.persist(extInstitution)).thenReturn(Uni.createFrom().item(updatedInstitution));
    });

    asserter.assertNotNull(() -> service.update(id.toString(), req));

    asserter.surroundWith(u -> Panache.withSession(() -> u));
  }
}
