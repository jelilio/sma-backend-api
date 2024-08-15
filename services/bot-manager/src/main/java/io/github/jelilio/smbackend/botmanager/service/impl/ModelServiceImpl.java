package io.github.jelilio.smbackend.botmanager.service.impl;

import io.github.jelilio.smbackend.botmanager.entity.Model;
import io.github.jelilio.smbackend.botmanager.entity.enumeration.ModelSource;
import io.github.jelilio.smbackend.botmanager.entity.enumeration.ModelType;
import io.github.jelilio.smbackend.botmanager.service.ModelService;
import io.github.jelilio.smbackend.common.entity.enumeration.Language;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.common.utils.PaginationUtil;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;


@ApplicationScoped
public class ModelServiceImpl implements ModelService {
  private static final Logger logger = LoggerFactory.getLogger(ModelServiceImpl.class);

  @Override
  public Uni<Model> findById(String id) {
    return Model.findById(id).onItem().ifNull()
        .failWith(() -> new NotFoundException("Not found"));
  }

  @Override
  public Uni<Model> findByIdAndType(String id, ModelType type) {
    return Model.findById(id, type).onItem().ifNull()
        .failWith(() -> new NotFoundException("Not found"));
  }

  @Override
  public Uni<List<Model>> findAll(ModelType type) {

    var query = type == null? Model.<Model>findAll() : Model.find(type);

    return query.list();
  }

  @Override
  public Uni<List<Model>> findAll(ModelType type, Language lang) {
    if(type == null && lang == null) return Model.<Model>findAll().list();

    if(type != null && lang != null) return Model.find(type, lang).list();

    if(type == null) return Model.findByLang(lang).list();

    return Model.find(type).list();
  }

  @Override
  public Uni<List<Model>> findAllByIds(Set<String> ids) {
    return ids.isEmpty()? Uni.createFrom().item(emptyList()) : Model.findAllByIds(ids);
  }

  @Override
  public Uni<Paged<Model>> findAll(ModelType type, int size, int index) {
    Page page = Page.of(index, size);

    var query = type == null? Model.<Model>findAll() : Model.find(type);

    return PaginationUtil.paginate(page, query.page(page));
  }

  @Override
  public Uni<List<String>> breakSentences(String content) {
    return Model.findPrimary(ModelType.SENTENCE).flatMap(it -> breakSentences(content, it));
  }

//  @Override
//  public Uni<List<String>> breakSentences(String content, Model model) {
//    return check(content, model);
//  }

  public Multi<String> breakSentences22(String content) {
    Uni<List<String>> unis = Model.findPrimary(ModelType.SENTENCE).flatMap(it -> breakSentences(content, it));

    return unis.toMulti().flatMap(list -> Multi.createFrom().iterable(list));
  }

  @Override
  public Multi<String> breakSentencesToList22(String content, Model model) {
    var list = breakSentencesToList(content, model);
    return Multi.createFrom().iterable(list);
  }

  @Override
  public Uni<List<String>> breakSentences(String content, Model model) {
    logger.info("check: {}", model);
    if (model.source == ModelSource.CLASSPATH) {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      try {
        URL modelIn = classloader.getResource(model.location);
        if(modelIn == null) return Uni.createFrom().item(emptyList());

        SentenceDetectorME myCategorizer = new SentenceDetectorME(new SentenceModel(modelIn));
        String[] sentences = myCategorizer.sentDetect(content);

        return Uni.createFrom().item(Arrays.asList(sentences));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return Uni.createFrom().item(emptyList());
  }

  // TODO:: BREAK THE SENTENCE, PUSH INTO A QUEUE

  @Override
  public List<String> breakSentencesToList(String content, Model model) {
    logger.info("breakSentencesToArray: {}", model);
    if (model.source == ModelSource.CLASSPATH) {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      try {
        URL modelIn = classloader.getResource(model.location);
        if(modelIn == null) return emptyList();

        SentenceDetectorME myCategorizer = new SentenceDetectorME(new SentenceModel(modelIn));
        String[] sentences = myCategorizer.sentDetect(content);

        return Arrays.asList(sentences);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return emptyList();
  }

  @Override
  public Uni<List<String>> tokenizeSentences(String content, Model model) {
    logger.info("tokenizeSentences: {}", model);
    if (model.source == ModelSource.CLASSPATH) {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      try {
        URL modelIn = classloader.getResource(model.location);
        if(modelIn == null) return Uni.createFrom().item(emptyList());

        TokenizerME myCategorizer = new TokenizerME(new TokenizerModel(modelIn));
        String[] tokens = myCategorizer.tokenize(content);

        return Uni.createFrom().item(Arrays.asList(tokens));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return Uni.createFrom().item(emptyList());
  }

  @Override
  public List<String> tokenizeSentencesToList(String sentence, Model model) {
    if (model.source == ModelSource.CLASSPATH) {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      try {
        URL modelIn = classloader.getResource(model.location);
        if(modelIn == null) return emptyList();

        TokenizerME myCategorizer = new TokenizerME(new TokenizerModel(modelIn));
        String[] tokens = myCategorizer.tokenize(sentence);

        return Arrays.asList(tokens);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return emptyList();
  }

  @Override
  public List<String> detectPOSTags(List<String> tokens, Model model) {
    if (model.source == ModelSource.CLASSPATH) {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      try {
        URL modelIn = classloader.getResource(model.location);
        if(modelIn == null) return emptyList();

        POSTaggerME myCategorizer = new POSTaggerME(new POSModel(modelIn));
        String[] posTokens = myCategorizer.tag(tokens.toArray(new String[0]));

        return Arrays.asList(posTokens);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return emptyList();
  }

  @Override
  public List<String> lemmatizeTokens(List<String> tokens, List<String> posTags, Model model) {
    if (model.source == ModelSource.CLASSPATH) {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      try {
        URL modelIn = classloader.getResource(model.location);
        if(modelIn == null) return emptyList();

        LemmatizerME myCategorizer = new LemmatizerME(new LemmatizerModel(modelIn));
        String[] posTokens = myCategorizer.lemmatize(tokens.toArray(new String[0]), posTags.toArray(new String[0]));

        return Arrays.asList(posTokens);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return emptyList();
  }
}