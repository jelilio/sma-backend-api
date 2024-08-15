package io.github.jelilio.smbackend.botmanager.service;

import io.github.jelilio.smbackend.botmanager.entity.Bot;
import io.github.jelilio.smbackend.botmanager.entity.BotAction;
import io.github.jelilio.smbackend.common.dto.BotDto;
import io.github.jelilio.smbackend.common.dto.response.PostRes;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.common.utils.Pair;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface BotService {
  Uni<Bot> findById(String id);

  Uni<Boolean> checkIfNameAvailable(String name);

  Uni<Paged<Bot>> findAll(int size, int index);

  Uni<Bot> createBot(BotDto botDto);

  Uni<Bot> updateBot(String id, BotDto botDto);

  Uni<List<Pair<BotAction, String>>> analyzePost(PostRes post);
}
