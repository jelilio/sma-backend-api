package io.github.jelilio.smbackend.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public class FileDto {
    @NotNull
    @FormParam("file")
//    @PartFilename("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public FileUpload file;

    @RestForm
    @NotNull @NotBlank @NotEmpty
    public String institutionId;
}
