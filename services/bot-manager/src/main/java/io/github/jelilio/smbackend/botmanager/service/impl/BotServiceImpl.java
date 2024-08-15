package io.github.jelilio.smbackend.botmanager.service.impl;

import io.github.jelilio.smbackend.botmanager.entity.Bot;
import io.github.jelilio.smbackend.botmanager.entity.BotAction;
import io.github.jelilio.smbackend.botmanager.entity.Categorizer;
import io.github.jelilio.smbackend.botmanager.entity.Model;
import io.github.jelilio.smbackend.botmanager.entity.enumeration.ModelType;
import io.github.jelilio.smbackend.botmanager.service.BotService;
import io.github.jelilio.smbackend.botmanager.service.CategorizerService;
import io.github.jelilio.smbackend.botmanager.service.ModelService;
import io.github.jelilio.smbackend.common.dto.BotDto;
import io.github.jelilio.smbackend.common.dto.response.PostRes;
import io.github.jelilio.smbackend.common.exception.AlreadyExistException;
import io.github.jelilio.smbackend.common.exception.BadRequestException;
import io.github.jelilio.smbackend.common.exception.NotFoundException;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.common.utils.PaginationUtil;
import io.github.jelilio.smbackend.common.utils.Pair;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
  public Uni<Bot> findById(String id) {
    return Bot.<Bot>findById(id).onItem().ifNull()
        .failWith(() -> new NotFoundException("Not found"));
  }

  @Override
  public Uni<Boolean> checkIfNameAvailable(String name) {
    return Bot.countByName(name)
        .onItem().transform(count -> count == 0);
  }

  @Override
  public Uni<Paged<Bot>> findAll(int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, Bot.findAll().page(page));
  }

  public Uni<Boolean> checkIfNameExist(String name) {
    return Bot.countByName(name)
        .onItem().transform(count -> count > 0);
  }

  public Uni<Boolean> checkIfNameExistButNotId(String id, String name) {
    return Bot.countByNameNotId(id, name)
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

  @Override
  public Uni<Bot> createBot(BotDto botDto) {
    Uni<Boolean> uniNameUsed = checkIfNameIsUsed.apply(null, botDto.name());
    Uni<Categorizer> uniCategorizer = categorizerService.findById(botDto.categorizer().id());

    return Panache.withTransaction(() -> {
      return uniNameUsed.flatMap(avail -> {
        return uniCategorizer.flatMap(categorizer -> {
          return verify(botDto).flatMap(models -> {
            Bot bot = new Bot(botDto.name(), botDto.lang(), botDto.description(), categorizer, new HashSet<>(models));
            return Panache.withTransaction(bot::persist);
          });
        });
      });
    });
  }

  private Uni<List<Model>> verify(BotDto botDto) {
    Uni<List<Model>> uniModels = modelService.findAllByIds(botDto.getModelIds());
    return uniModels.flatMap(models -> {
      BiFunction<ModelType, List<Model>, Optional<Model>> mod = (ModelType type, List<Model> allModels) ->
          allModels.stream().filter(it -> it.type == type).findFirst();

      if(mod.apply(ModelType.TOKENIZER, models).isEmpty()) {
        return Uni.createFrom().failure(new BadRequestException("Tokenizer model is required"));
      }

      if(mod.apply(ModelType.SENTENCE, models).isEmpty()) {
        return Uni.createFrom().failure(new BadRequestException("Sentence model is required"));
      }

      if(mod.apply(ModelType.LEMMATIZER, models).isEmpty()) {
        return Uni.createFrom().failure(new BadRequestException("Lemmatizer model is required"));
      }

     return Uni.createFrom().item(models);
    });
  }

  @Override
  public Uni<Bot> updateBot(String id, BotDto botDto) {
    Uni<Bot> uniExtBot = findById(id);
    Uni<Boolean> uniNameUsed = checkIfNameIsUsed.apply(id, botDto.name());
    Uni<Categorizer> uniCategorizer = categorizerService.findById(botDto.categorizer().id());

    return uniExtBot.flatMap(extBot -> {
      return uniNameUsed.flatMap(nameIsUsed -> {
        return uniCategorizer.flatMap(categorizer -> {
          return verify(botDto).flatMap(models -> {
            extBot.name = botDto.name();
            extBot.description = botDto.description();
            extBot.lang = botDto.lang();
            extBot.categorizer = categorizer;
            extBot.models = new HashSet<>(models);

            return Panache.withTransaction(extBot::persist);
          });
        });
      });
    });
  }

  private String detectCategory(List<String> finalTokens, DoccatModel model)  {

    // Initialize document categorizer tool
    DocumentCategorizerME myCategorizer = new DocumentCategorizerME(model);

    // Get best possible category.
    double[] probabilitiesOfOutcomes = myCategorizer.categorize(finalTokens.toArray(new String[0]));
    String category = myCategorizer.getBestCategory(probabilitiesOfOutcomes);

//    System.out.println("Category: " + category + "; percent: " );
//    System.out.println("getAllResults: " + myCategorizer.getAllResults(probabilitiesOfOutcomes));
//    System.out.println("getNumberOfCategories: " + myCategorizer.getNumberOfCategories());
//    System.out.println("sortedScoreMap: " + myCategorizer.sortedScoreMap(finalTokens));
//    for(int i=0; i<probabilitiesOfOutcomes.length; i++) {
//      System.out.print("score: " + probabilitiesOfOutcomes[i] + "; ");
//    }

    return category;
  }

  @Override
  public Uni<List<Pair<BotAction, String>>> analyzePost(PostRes post) {
    Uni<List<BotAction>> botActions = BotAction.findAll().list();

    return botActions.flatMap(it -> {
      logger.info("total botActions: {}", it.size());
      return analyzePost(post, it);
    });
  }

  public Uni<List<Pair<BotAction, String>>> analyzePost(PostRes post, List<BotAction> botActions) {
    Map<Bot, List<BotAction>> itemsPro = botActions.stream()
        .filter(r -> r.bot != null)
        .collect(Collectors.groupingBy(it -> it.bot));

    List<Pair<BotAction, String>> result = new ArrayList<>();
    itemsPro.forEach((bot, actions) -> {
      var categories = analyzePost(post, bot, actions);
      result.addAll(categories);
    });

    return Uni.createFrom().item(result);
  }

  public List<Pair<BotAction, String>> analyzePost(PostRes post, Bot bot, List<BotAction> botActions) {
    // 1. get post caption; i.e. post.caption
    // 2. break the caption into sentences using the bot.getSentence() model
    // 3. loop through each sentence and tokenize it using the bot.getTokenizer() model

    var categorizerItems = botActions.stream().map(it -> it.item).toList();

    var doccatModel = categorizerService.trainedModel(bot.lang.name(), categorizerItems);

    String content = post.caption;
    var sentences = modelService.breakSentencesToList(content, bot.getSentence());

    List<Pair<BotAction, String>> categories = sentences.stream().map(sentence -> {
      var tokens = modelService.tokenizeSentencesToList(sentence, bot.getTokenizer());

      var posTags = modelService.detectPOSTags(tokens, bot.getPostagger());

      var lemmas = modelService.lemmatizeTokens(tokens, posTags, bot.getLemmatizer());

      String category = detectCategory(lemmas, doccatModel);

      var action = botActions.stream().filter(it -> it.item.name.equalsIgnoreCase(category))
          .findAny().orElse(null);

      return Pair.of(action, category);
    }).toList();

    return categories;
  }

//  public Uni<Void> analyzePost22(PostRes post) {
//    Uni<List<BotAction>> unis = BotAction.findAll().list();
//
//    return unis.flatMap(list -> {
//      Map<Bot, List<BotAction>> itemsPro = list.stream()
//          .filter(r -> r.bot != null)
//          .collect(Collectors.groupingBy(it -> it.bot));
//
//      itemsPro.forEach((bot, actions) -> {
//
//
//      });
//    });
//  }

  public Uni<List<Pair<BotAction, String>>> analyzePost22(PostRes post, Bot bot, List<BotAction> botActions) {
    var categorizerItems = botActions.stream().map(it -> it.item).toList();

//    var uniDoccatModel = categorizerService.trainedModel22(bot.lang.name(), categorizerItems);

    var unis = modelService.breakSentencesToList22(post.caption, bot.getSentence());

    return unis.map(sentence -> {
      var doccatModel = categorizerService.trainedModel(bot.lang.name(), categorizerItems);

      var tokens = modelService.tokenizeSentencesToList(sentence, bot.getTokenizer());

      var posTags = modelService.detectPOSTags(tokens, bot.getPostagger());

      var lemmas = modelService.lemmatizeTokens(tokens, posTags, bot.getLemmatizer());

      String category = detectCategory(lemmas, doccatModel);

      var action = botActions.stream().filter(it -> it.item.name.equalsIgnoreCase(category))
          .findAny().orElse(null);

      var pair = Pair.of(action, category);

      return pair;
    }).collect().asList();
  }
}
