package io.github.jelilio.smbackend.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

import java.io.File;

public class UserUpdateDto {
  @FormParam("image")
//  @PartFilename("image")
  @PartType(MediaType.APPLICATION_OCTET_STREAM)
  public File file;

  @RestForm
  @NotNull @NotBlank
  public String name;

  @RestForm
  @NotNull @NotBlank @Email
  public String email;
}
