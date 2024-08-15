package io.github.jelilio.smbackend.usermanager.queue.model;

import io.github.jelilio.smbackend.common.dto.RegisterDto;
import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Set;

@RegisterForReflection
public record RegisterUser (
    String id,
    RegisterDto dto,
    Set<String> roles,
    UserType type
){
}
