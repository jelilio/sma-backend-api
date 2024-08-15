package io.github.jelilio.smbackend.botmanager.service.impl;

import io.github.jelilio.smbackend.botmanager.dto.BotDto;
import io.github.jelilio.smbackend.botmanager.entity.Bot;
import io.github.jelilio.smbackend.botmanager.entity.BotAction;
import io.github.jelilio.smbackend.botmanager.entity.Categorizer;
import io.github.jelilio.smbackend.botmanager.entity.Model;
import io.github.jelilio.smbackend.botmanager.entity.enumeration.ModelType;
import io.github.jelilio.smbackend.botmanager.service.BotService;
import io.github.jelilio.smbackend.botmanager.service.CategorizerService;
import io.github.jelilio.smbackend.botmanager.service.ModelService;
import io.github.jelilio.smbackend.botmanager.utils.Paged;
import io.github.jelilio.smbackend.botmanager.utils.PaginationUtil;
import io.github.jelilio.smbackend.botmanager.exception.AlreadyExistException;
import io.github.jelilio.smbackend.botmanager.exception.BadRequestException;
import io.github.jelilio.smbackend.botmanager.exception.NotFoundException;
import io.github.jelilio.smbackend.commonutil.dto.response.AnalysedObject;
import io.github.jelilio.smbackend.commonutil.dto.response.PostObject;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.Action;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.Severity;
import io.github.jelilio.smbackend.commonutil.utils.Tripple;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@ApplicationScoped
public class BotServiceImpl implements BotService {
  private static final Logger logger = LoggerFactory.getLogger(BotService.class);

  @Inject
  ModelService modelService;
  @Inject
  CategorizerService categorizerService;

  @Override
  public Bot findById(String id) {
    return Bot.findById(id).orElseThrow(() -> new NotFoundException("Not found"));
  }

  @Override
  public Boolean checkIfNameAvailable(String name) {
    return Bot.countByName(name) == 0;
  }

  @Override
  public Paged<Bot> findAll(int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, Bot.findAll(Sort.descending("createdDate")).page(page));
  }

  public Boolean checkIfNameExist(String name) {
    return Bot.countByName(name) > 0;
  }

  public Boolean checkIfNameExistButNotId(String id, String name) {
    return Bot.countByNameNotId(id, name) > 0;
  }

  BiFunction<String, String, Boolean> checkIfNameIsUsed = (String id, String name) -> {
    if (name == null) return null;

    return id == null ? checkIfNameExist(name) : checkIfNameExistButNotId(id, name);
  };

  @Override
  @Transactional
  public Bot createBot(BotDto botDto) {
    Boolean uniNameUsed = checkIfNameIsUsed.apply(null, botDto.name());

    if(uniNameUsed == null) {
      throw new BadRequestException("Name is required");
    }

    if(uniNameUsed) {
      throw new AlreadyExistException("Bot with name " + botDto.name() + " already exists");
    }

    Categorizer categorizer = categorizerService.findById(botDto.categorizer().id());

    List<Model> models = verify(botDto);

    Bot bot = new Bot(botDto.name(), botDto.lang(), botDto.description(), categorizer, new HashSet<>(models));
    bot.persist();

    return bot;
  }

  private List<Model> verify(BotDto botDto) {
    List<Model> models = modelService.findAllByIds(botDto.getModelIds());

    BiFunction<ModelType, List<Model>, Optional<Model>> mod = (ModelType type, List<Model> allModels) ->
        allModels.stream().filter(it -> it.type == type).findFirst();

    if (mod.apply(ModelType.TOKENIZER, models).isEmpty()) {
      throw new BadRequestException("Tokenizer model is required");
    }

    if (mod.apply(ModelType.SENTENCE, models).isEmpty()) {
      throw new BadRequestException("Sentence model is required");
    }

    if (mod.apply(ModelType.LEMMATIZER, models).isEmpty()) {
      throw new BadRequestException("Lemmatizer model is required");
    }

    return models;
  }

  @Override
  @Transactional
  public Bot updateBot(String id, BotDto botDto) {
    Bot extBot = findById(id);

    Boolean uniNameUsed = checkIfNameIsUsed.apply(id, botDto.name());

    if(uniNameUsed == null) {
      throw new BadRequestException("Name is required");
    }

    if(uniNameUsed) {
      throw new AlreadyExistException("Bot with name " + botDto.name() + " already exists");
    }

    Categorizer categorizer = categorizerService.findById(botDto.categorizer().id());

    List<Model> models = verify(botDto);

    extBot.name = botDto.name();
    extBot.description = botDto.description();
    extBot.lang = botDto.lang();
    extBot.categorizer = categorizer;
    extBot.models = new HashSet<>(models);

    return extBot;
  }

  private String detectCategory(List<String> finalTokens, DoccatModel model) {

    // Initialize document categorizer tool
    DocumentCategorizerME myCategorizer = new DocumentCategorizerME(model);

    // Get best possible category.
    double[] probabilitiesOfOutcomes = myCategorizer.categorize(finalTokens.toArray(new String[0]));

    String category = myCategorizer.getBestCategory(probabilitiesOfOutcomes);

    System.out.println("Category: " + category + "; percent: " );
    System.out.println("getAllResults: " + myCategorizer.getAllResults(probabilitiesOfOutcomes));
    System.out.println("getNumberOfCategories: " + myCategorizer.getNumberOfCategories());
    System.out.println("sortedScoreMap: " + myCategorizer.sortedScoreMap(finalTokens.toArray(new String[0])));

    return category;
  }

  @Override
  public AnalysedObject analyzePost(PostObject post) {
    List<BotAction> botActions = BotAction.findAll().list();

    return analyzePost(post, botActions);
  }

  public AnalysedObject analyzePost(PostObject post, List<BotAction> botActions) {
    Map<Bot, List<BotAction>> itemsPro = botActions.stream()
        .filter(r -> r.bot != null)
        .collect(Collectors.groupingBy(it -> it.bot));

    List<Tripple<String, Severity, Action>> result = new ArrayList<>();
    itemsPro.forEach((bot, actions) -> {
      var categories = analyzePost(post, bot, actions);
      result.addAll(categories);
    });

    return new AnalysedObject(post, result);
  }

  public List<Tripple<String, Severity, Action>> analyzePost(PostObject post, Bot bot, List<BotAction> botActions) {
    // 1. get post caption; i.e. post.caption
    // 2. break the caption into sentences using the bot.getSentence() model
    // 3. loop through each sentence and tokenize it using the bot.getTokenizer() model

    var categorizerItems = botActions.stream().map(it -> it.item).toList();

    var doccatModel = categorizerService.trainedModel(bot.lang.name(), categorizerItems);

    String content = post.caption();
    var sentences = modelService.breakSentencesToList(content, bot.getSentence());

    return sentences.stream().map(sentence -> {
      var tokens = modelService.tokenizeSentencesToList(sentence, bot.getTokenizer());

      var posTags = modelService.detectPOSTags(tokens, bot.getPostagger());

      var lemmas = modelService.lemmatizeTokens(tokens, posTags, bot.getLemmatizer());

      String category = detectCategory(lemmas, doccatModel);

      var botAction = botActions.stream().filter(it -> it.item.name.equalsIgnoreCase(category))
          .findAny().orElse(null);

      var action = botAction != null? botAction.action : null;
      var severity = botAction != null? botAction.severity : null;

      return Tripple.of(category, severity, action);
    }).filter(it -> it.right() != null && Action.NONE != it.right()) // discard null and NONE action
        .toList();
  }
}

