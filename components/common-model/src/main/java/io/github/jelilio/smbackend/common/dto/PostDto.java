package io.github.jelilio.smbackend.common.dto;

import jakarta.validation.constraints.Size;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

import java.io.File;

public class PostDto{
    @RestForm
    @Size(max = 150, min = 5)
    public String caption;

    @FormParam("everyone")
    public Boolean everyone;

    @FormParam("recipientId")
    public String recipientId;

    @FormParam("image")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public File file;

    public String caption() {
        return caption;
    }

    public File file() {
        return file;
    }
}
