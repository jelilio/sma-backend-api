package io.github.jelilio.smbackend.common.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.PartType;

import java.io.File;

public class PhotoDto {
    @NotNull
    @FormParam("image")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    File file;

    public File file() {
        return file;
    }
}
