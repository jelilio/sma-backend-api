package io.github.jelilio.smbackend.botmanager.service;

import io.github.jelilio.smbackend.botmanager.dto.BotDto;
import io.github.jelilio.smbackend.botmanager.entity.Bot;
import io.github.jelilio.smbackend.botmanager.utils.Paged;
import io.github.jelilio.smbackend.commonutil.dto.response.AnalysedObject;
import io.github.jelilio.smbackend.commonutil.dto.response.PostObject;

public interface BotService {
  Bot findById(String id);

  Boolean checkIfNameAvailable(String name);

  Paged<Bot> findAll(int size, int index);

  Bot createBot(BotDto botDto);

  Bot updateBot(String id, BotDto botDto);

  AnalysedObject analyzePost(PostObject post);
}
