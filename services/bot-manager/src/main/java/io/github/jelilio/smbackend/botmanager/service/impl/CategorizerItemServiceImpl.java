package io.github.jelilio.smbackend.botmanager.service.impl;

import io.github.jelilio.smbackend.botmanager.entity.Categorizer;
import io.github.jelilio.smbackend.botmanager.entity.CategorizerItem;
import io.github.jelilio.smbackend.botmanager.service.CategorizerItemService;
import io.github.jelilio.smbackend.common.dto.CategorizerItemDto;
import io.github.jelilio.smbackend.common.exception.AlreadyExistException;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.common.utils.PaginationUtil;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.function.TriFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApplicationScoped
public class CategorizerItemServiceImpl implements CategorizerItemService {
  private static final Logger logger = LoggerFactory.getLogger(CategorizerItemServiceImpl.class);

//  @Inject
//  EntityManager entityManager;

  @Override
  public Uni<CategorizerItem> findById(String id) {
    logger.info("findById: id: {}", id);
    return CategorizerItem.findById(id).onItem().ifNull()
        .failWith(() -> new NotFoundException("Categorizer not found"));
  }

  @Override
  public Uni<CategorizerItem> findById(Categorizer categorizer, String id) {
    logger.info("findById: id: {}, categorizer: {}", id, categorizer);
    return CategorizerItem.findById(categorizer, id).onItem().ifNull()
        .failWith(() -> new NotFoundException("Categorizer not found"));
  }

  public Uni<Boolean> checkIfNameExist(Categorizer categorizer, String name) {
    return CategorizerItem.countByName(categorizer, name)
        .onItem().transform(count -> count > 0);
  }

  public static Uni<Boolean> checkIfNameExistButNotId(Categorizer categorizer, String id, String name) {
    return CategorizerItem.countByNameNotId(categorizer, id, name)
        .onItem().transform(count -> count > 0);
  }

  TriFunction<Categorizer, String, String, Uni<Boolean>> checkIfNameIsUsed = (Categorizer categorizer, String id, String name) -> {
    if(name == null) return Uni.createFrom().failure(new Exception("Name cannot be null"));

    Uni<Boolean> uni = id == null? checkIfNameExist(categorizer, name) : checkIfNameExistButNotId(categorizer, id, name);

    return uni.flatMap(inUsed -> {
      if(inUsed) {
        logger.info("It is inused");
        return Uni.createFrom().failure(() -> new AlreadyExistException(String.format("Category with this name: %s, already exist", name)));
      }
      return Uni.createFrom().item(true);
    });
  };

  @Override
  public Uni<List<CategorizerItem>> findAllItems(Categorizer categorizer) {
    return  CategorizerItem.findAllByCategorizer(categorizer).list();
  }

  @Override
  public Uni<Paged<CategorizerItem>> findAllItems(Categorizer categorizer, int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, CategorizerItem.findAllByCategorizer(categorizer).page(page));
  }

  @Override
  public Uni<CategorizerItem> create(Categorizer categorizer, CategorizerItemDto dto) {
    Uni<Boolean> uniNameUsed = checkIfNameIsUsed.apply(categorizer, null, dto.name());

    return Panache.withTransaction(() ->
        uniNameUsed
            .flatMap(isUsed -> {
              CategorizerItem categorizerItem = new CategorizerItem(dto.name(), dto.sentences(), categorizer);
              return Panache.withTransaction(categorizerItem::persist);
            })
    );
  }

  @Override
  public Uni<CategorizerItem> update(Categorizer categorizer, String id, CategorizerItemDto dto) {
    logger.info("update: categorizer: {}, dto: {}", categorizer, dto);
    Uni<CategorizerItem> uniExtCategorizerItem = findById(categorizer, id);
    Uni<Boolean> uniNameUsed = checkIfNameIsUsed.apply(categorizer, id, dto.name());

    return uniExtCategorizerItem.flatMap(extCategorizerItem -> {
      logger.info("extCategorizerItem: id: {}", extCategorizerItem);

      return uniNameUsed
          .flatMap(nameIsUsed -> {
            logger.info("update: nameIsUsed: {}", nameIsUsed);
            extCategorizerItem.name = dto.name();
            extCategorizerItem.sentences = dto.sentences();
//            extCategorizerItem.categorizer = categorizer;
            return Panache.<CategorizerItem>withTransaction(extCategorizerItem::persist);
          });
    });
  }

  @Override
  public Uni<Void> delete(Categorizer categorizer, String id) {
    return Panache.withTransaction(() ->
        findById(categorizer, id).flatMap(PanacheEntityBase::delete)
    );
  }

//  public Iterable<CategorizerItem> findAll(Instant isDeleted){
//    Mutiny.Session session = entityManager.unwrap(Mutiny.Session.class);
//    Filter filter = session.enableFilter("deletedProductFilter");
//    filter.setParameter("isDeleted", isDeleted);
//    Iterable<CategorizerItem> products =  CategorizerItem.findAll();
//    session.disableFilter("deletedProductFilter");
//    return products;
//  }
}
