package io.github.jelilio.smbackend.botmanager.service.impl;

import io.github.jelilio.smbackend.botmanager.dto.CategorizerItemDto;
import io.github.jelilio.smbackend.botmanager.entity.Categorizer;
import io.github.jelilio.smbackend.botmanager.entity.CategorizerItem;
import io.github.jelilio.smbackend.botmanager.service.CategorizerItemService;
import io.github.jelilio.smbackend.botmanager.utils.Paged;
import io.github.jelilio.smbackend.botmanager.utils.PaginationUtil;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
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
  public CategorizerItem findById(String id) {
    logger.info("findById: id: {}", id);
    return CategorizerItem.findById(id)
        .orElseThrow(() -> new NotFoundException("Categorizer not found"));
  }

  @Override
  public CategorizerItem findById(Categorizer categorizer, String id) {
    logger.info("findById: id: {}, categorizer: {}", id, categorizer);
    return CategorizerItem.findById(categorizer, id)
        .orElseThrow(() -> new NotFoundException("Categorizer not found"));
  }

  public Boolean checkIfNameExist(Categorizer categorizer, String name) {
    return CategorizerItem.countByName(categorizer, name) > 0;
  }

  public static Boolean checkIfNameExistButNotId(Categorizer categorizer, String id, String name) {
    return CategorizerItem.countByNameNotId(categorizer, id, name) > 0;
  }

  TriFunction<Categorizer, String, String, Boolean> checkIfNameIsUsed = (Categorizer categorizer, String id, String name) -> {
    if(name == null) return null;

    return id == null? checkIfNameExist(categorizer, name) : checkIfNameExistButNotId(categorizer, id, name);
  };

  @Override
  public List<CategorizerItem> findAllItems(Categorizer categorizer) {
    return  CategorizerItem.findAllByCategorizer(categorizer).list();
  }

  @Override
  public Paged<CategorizerItem> findAllItems(Categorizer categorizer, int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, CategorizerItem.findAllByCategorizer(categorizer).page(page));
  }

  @Override
  @Transactional
  public CategorizerItem create(Categorizer categorizer, CategorizerItemDto dto) {
    Boolean uniNameUsed = checkIfNameIsUsed.apply(categorizer, null, dto.name());

    if(uniNameUsed == null) {
      throw new BadRequestException("Name can not be null");
    }

    if(uniNameUsed) {
      throw new BadRequestException("Name already in use");
    }

    CategorizerItem categorizerItem = new CategorizerItem(dto.name(), dto.sentences(), categorizer);
    categorizerItem.persist();

    return categorizerItem;
  }

  @Override
  @Transactional
  public CategorizerItem update(Categorizer categorizer, String id, CategorizerItemDto dto) {
    logger.info("update: categorizer: {}, dto: {}", categorizer, dto);
    CategorizerItem extCategorizerItem = findById(categorizer, id);

    Boolean uniNameUsed = checkIfNameIsUsed.apply(categorizer, id, dto.name());

    if(uniNameUsed == null) {
      throw new BadRequestException("Name can not be null");
    }

    if(uniNameUsed) {
      throw new BadRequestException("Name already in use");
    }

    extCategorizerItem.name = dto.name();
    extCategorizerItem.sentences = dto.sentences();
//    extCategorizerItem.persist();

    return extCategorizerItem;
  }

  @Override
  @Transactional
  public void delete(Categorizer categorizer, String id) {
    var categorizerItem = findById(categorizer, id);

    categorizerItem.delete();
  }
}
