package com.example.helloworld;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@Component
public class SwaggerUiStartupOpener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerUiStartupOpener.class);

    private final Environment environment;

    public SwaggerUiStartupOpener(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openSwaggerUiAfterStartup() {
        boolean enabled = Boolean.parseBoolean(
                environment.getProperty("swagger.open-browser-on-startup", "true"));

        if (!enabled) {
            LOGGER.info("Swagger browser auto-open is disabled.");
            return;
        }

        String configuredPath = environment.getProperty("springdoc.swagger-ui.path", "/swagger-ui.html");
        String swaggerUiPath = configuredPath.startsWith("/") ? configuredPath : "/" + configuredPath;

        String port = environment.getProperty(
                "local.server.port",
                environment.getProperty("server.port", "8080"));

        String swaggerUrl = "http://localhost:" + port + swaggerUiPath;

        if (GraphicsEnvironment.isHeadless() || !Desktop.isDesktopSupported()) {
            LOGGER.info("Desktop browser not available. Open Swagger manually at {}", swaggerUrl);
            return;
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            LOGGER.info("Desktop browse action is not supported. Open Swagger manually at {}", swaggerUrl);
            return;
        }

        try {
            desktop.browse(URI.create(swaggerUrl));
            LOGGER.info("Opened Swagger UI in browser: {}", swaggerUrl);
        } catch (IOException ex) {
            LOGGER.warn("Could not open browser automatically. Open Swagger manually at {}", swaggerUrl, ex);
        }
    }
}

