package io.github.jelilio.smbackend.newsfeed;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;

@OpenAPIDefinition(
    info = @Info(
        title = "Social-Media Newsfeed API",
        description = "This API allows provides endpoints to consumes and interact with the application Backend",
        version = "1.0-SNAPSHOT",
        contact = @Contact(name = "Jelili Adesina", url = "https://github.com/jelilio")
    )
)
public class MainApplication extends Application {

}
