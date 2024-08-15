package io.github.jelilio.smbackend.botmanager.service.impl;

import io.github.jelilio.smbackend.botmanager.entity.Categorizer;
import io.github.jelilio.smbackend.botmanager.entity.CategorizerItem;
import io.github.jelilio.smbackend.botmanager.service.CategorizerService;
import io.github.jelilio.smbackend.botmanager.utils.StringInputStreamFactory;
import io.github.jelilio.smbackend.common.dto.CategorizerDto;
import io.github.jelilio.smbackend.common.entity.enumeration.Language;
import io.github.jelilio.smbackend.common.exception.AlreadyExistException;
import io.github.jelilio.smbackend.common.exception.BadRequestException;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.common.utils.PaginationUtil;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
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
  public Uni<Categorizer> findById(String id) {
    return Categorizer.findById(id).onItem().ifNull()
        .failWith(() -> new NotFoundException("Categorizer not found"));
  }

  @Override
  public Uni<List<Categorizer>> findAll(Language language) {
    if(language != null) return Categorizer.findByLanguage(language).list();

    return Categorizer.findAll().list();
  }

  @Override
  public Uni<Paged<Categorizer>> findAll(int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, Categorizer.findAll().page(page));
  }

  public Uni<Boolean> checkIfNameExist(String name) {
    return Categorizer.countByName(name)
        .onItem().transform(count -> count > 0);
  }

  public Uni<Boolean> checkIfNameExistButNotId(String id, String name) {
    return Categorizer.countByNameNotId(id, name)
        .onItem().transform(count -> count > 0);
  }

  BiFunction<String, String, Uni<Boolean>> checkIfNameIsUsed = (String id, String name) -> {
    if(name == null) return Uni.createFrom().failure(new Exception("Name cannot be null"));

    Uni<Boolean> uni = id == null? checkIfNameExist(name) : checkIfNameExistButNotId(id, name);

    return uni.flatMap(inUsed -> {
      if(inUsed) {
        return Uni.createFrom().failure(() -> new AlreadyExistException(String.format("Categorizer with this name: %s, already exist", name)));
      }
      return Uni.createFrom().item(true);
    });
  };

  /*
  @Deprecated
  public Uni<Categorizer> create_(CategorizerDto dto) {
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
  public Uni<Categorizer> create(CategorizerDto dto) {
    Uni<Boolean> uniNameUsed = checkIfNameIsUsed.apply(null, dto.name());

    return Panache.withTransaction(() ->
        uniNameUsed
            .flatMap(isUsed -> {
              Categorizer categorizer = new Categorizer(dto.name(), dto.description(), dto.lang());
              return Panache.withTransaction(categorizer::persist);
            })
    );
  }

  @Override
  public Uni<Categorizer> update(String id, CategorizerDto dto) {
    Uni<Categorizer> uniExtCategorizer = findById(id);
    Uni<Boolean> uniNameUsed = checkIfNameIsUsed.apply(id, dto.name());

    return uniExtCategorizer.flatMap(extCategorizer -> {
      return uniNameUsed
          .flatMap(nameIsUsed -> {
            extCategorizer.name = dto.name();
            extCategorizer.lang = dto.lang();
            extCategorizer.description = dto.description();
            return Panache.withTransaction(extCategorizer::persist);
          });
    });
  }

  @Override
  public Uni<Void> delete(String id) {
    return Panache.withTransaction(() ->
        findById(id).flatMap(PanacheEntityBase::delete)
    );
  }

  @Override
  public Uni<Void> trainModel() {
    Set<CategorizerItem> lineItems = new HashSet<>();

    lineItems.add(new CategorizerItem(UUID.randomUUID(), "arsehole", "arsehole asshole"));
    lineItems.add(new CategorizerItem(UUID.randomUUID(), "big-black-cock", "big black cock"));
    lineItems.add(new CategorizerItem(UUID.randomUUID(), "bitch", "she is a bitch"));
    lineItems.add(new CategorizerItem(UUID.randomUUID(), "blowjob", "blowjob"));
    lineItems.add(new CategorizerItem(UUID.randomUUID(), "bugger", "bugger"));

    return trainedModel(lineItems);
  }

  public Uni<Void> trainedModel(Set<CategorizerItem> lineItems) {
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
//      throw new RuntimeException(e);
      return Uni.createFrom().failure(new BadRequestException(e.getMessage()));
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
//      throw new RuntimeException(e);
      return Uni.createFrom().failure(new BadRequestException(e.getMessage()));
    }

    return Uni.createFrom().voidItem();
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

  @Override
  public Uni<DoccatModel> trainedModel22(String lang, List<CategorizerItem> lineItems) {
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
      return Uni.createFrom().failure(new BadRequestException(e.getMessage()));
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
      return Uni.createFrom().failure(new BadRequestException(e.getMessage()));
    }

    return Uni.createFrom().item(model);
  }
}
