package io.github.jelilio.smbackend.botmanager.service.impl;

import io.github.jelilio.smbackend.botmanager.dto.CategorizerDto;
import io.github.jelilio.smbackend.botmanager.entity.Categorizer;
import io.github.jelilio.smbackend.botmanager.entity.CategorizerItem;
import io.github.jelilio.smbackend.botmanager.entity.enumeration.Language;
import io.github.jelilio.smbackend.botmanager.exception.AlreadyExistException;
import io.github.jelilio.smbackend.botmanager.exception.BadRequestException;
import io.github.jelilio.smbackend.botmanager.service.CategorizerService;
import io.github.jelilio.smbackend.botmanager.utils.Paged;
import io.github.jelilio.smbackend.botmanager.utils.PaginationUtil;
import io.github.jelilio.smbackend.botmanager.utils.StringInputStreamFactory;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import opennlp.tools.doccat.*;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@ApplicationScoped
public class CategorizerServiceImpl implements CategorizerService {
  private static final Logger logger = LoggerFactory.getLogger(CategorizerService.class);

  @Override
  public Categorizer findById(String id) {
    return Categorizer.findById(id).orElseThrow(() -> new NotFoundException("Categorizer Not found"));
  }

  @Override
  public List<Categorizer> findAll(Language language) {
    if(language != null) return Categorizer.findByLanguage(language).list();

    return Categorizer.findAll().list();
  }

  @Override
  public Paged<Categorizer> findAll(int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, Categorizer.findAll(Sort.descending("createdDate")).page(page));
  }

  public Boolean checkIfNameExist(String name) {
    return Categorizer.countByName(name) > 0;
  }

  public Boolean checkIfNameExistButNotId(String id, String name) {
    return Categorizer.countByNameNotId(id, name) > 0;
  }

  BiFunction<String, String, Boolean> checkIfNameIsUsed = (String id, String name) -> {
    if(name == null) return null;

    return id == null? checkIfNameExist(name) : checkIfNameExistButNotId(id, name);
  };

  /*
  @Deprecated
  public Categorizer create_(CategorizerDto dto) {
    Uni<Boolean> uniCheckNameExist = checkIfNameExist(dto.name());

    return uniCheckNameExist.flatMap(nameExist -> {
      if(nameExist) {
        return Uni.createFrom().failure(new BadRequestException("Name already exist"));
      }

      var items = dto.content();
      if(items.isEmpty())
        return Uni.createFrom().failure(new BadRequestException("Content cannot be empty"));

      Categorizer categorizer = new Categorizer(dto.name(), dto.description(), dto.lang());

      Set<CategorizerItem> categorizerItems = items.stream().map(it -> new CategorizerItem(
          it.name().trim().replaceAll(" +", "-"),
          it.sentences().trim().replaceAll("\\R", " "),
          categorizer
      )).collect(Collectors.toSet());

      categorizer.categorizerItems.addAll(categorizerItems);

      return Panache.withTransaction(categorizer::persist);
    });
  }*/

  @Override
  @Transactional
  public Categorizer create(CategorizerDto dto) {
    if(dto.name() == null) {
      throw new NotFoundException("Name cannot be null");
    }

    Boolean inUsed = checkIfNameIsUsed.apply(null, dto.name());

    if(inUsed) {
      throw new AlreadyExistException(String.format("Categorizer with this name: %s, already exist", dto.name()));
    }

    Categorizer categorizer = new Categorizer(dto.name(), dto.description(), dto.lang());
    categorizer.persist();

    return categorizer;
  }

  @Override
  @Transactional
  public Categorizer update(String id, CategorizerDto dto) {
    if(dto.name() == null) {
      throw new NotFoundException("Name cannot be null");
    }

    Boolean inUsed = checkIfNameIsUsed.apply(id, dto.name());

    Categorizer extCategorizer = findById(id);

    if(inUsed) {
      throw new AlreadyExistException(String.format("Categorizer with this name: %s, already exist", dto.name()));
    }

    extCategorizer.name = dto.name();
    extCategorizer.lang = dto.lang();
    extCategorizer.description = dto.description();
//    extCategorizer.persist();

    return extCategorizer;
  }

  @Override
  @Transactional
  public void delete(String id) {
    Categorizer categorizer = findById(id);
    categorizer.delete();
  }

  @Override
  public void trainModel() {
    Set<CategorizerItem> lineItems = new HashSet<>();

    lineItems.add(new CategorizerItem(UUID.randomUUID(), "arsehole", "arsehole asshole"));
    lineItems.add(new CategorizerItem(UUID.randomUUID(), "big-black-cock", "big black cock"));
    lineItems.add(new CategorizerItem(UUID.randomUUID(), "bitch", "she is a bitch"));
    lineItems.add(new CategorizerItem(UUID.randomUUID(), "blowjob", "blowjob"));
    lineItems.add(new CategorizerItem(UUID.randomUUID(), "bugger", "bugger"));

    trainedModel(lineItems);
  }

  public void trainedModel(Set<CategorizerItem> lineItems) {
    Set<String> items = lineItems.stream()
        .map(it -> String.format("%s %s",
            it.name.trim().replaceAll(" +", "-"),
            it.sentences.trim().replaceAll("\\R", " ")
        )).collect(Collectors.toSet());

    String content = String.join("\n", items);

    logger.info("trainedModel: content: {}", content);

    InputStreamFactory inputStreamFactory = new StringInputStreamFactory(content);
    ObjectStream<String> lineStream = null;
    try {
      lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new BadRequestException(e.getMessage());
    }
    ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);

    DoccatFactory factory = new DoccatFactory(new FeatureGenerator[] { new BagOfWordsFeatureGenerator() });

    TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
    params.put(TrainingParameters.CUTOFF_PARAM, 0);

    // Train a model with classifications from above file.
    try {
      DoccatModel model = DocumentCategorizerME.train("eng", sampleStream, params, factory);
      File file = new File("eng-cat-model.bin");
      model.serialize(file);
    } catch (IOException e) {
      throw new BadRequestException(e.getMessage());
    }
  }

  @Override
  public DoccatModel trainedModel(String lang, List<CategorizerItem> lineItems) {
    Set<String> items = lineItems.stream()
        .map(it -> String.format("%s %s",
            it.name.trim().replaceAll(" +", "-"),
            it.sentences.trim().replaceAll("\\R", " ")
        )).collect(Collectors.toSet());

    String content = String.join("\n", items);


    InputStreamFactory inputStreamFactory = new StringInputStreamFactory(content);
    ObjectStream<String> lineStream = null;
    try {
      lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);

    DoccatFactory factory = new DoccatFactory(new FeatureGenerator[] { new BagOfWordsFeatureGenerator() });

    TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
    params.put(TrainingParameters.CUTOFF_PARAM, 0);

    // Train a model with classifications from above file.
    DoccatModel model = null;
    try {
      model = DocumentCategorizerME.train(lang, sampleStream, params, factory);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return model;
  }
}
