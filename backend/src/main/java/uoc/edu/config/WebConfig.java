package uoc.edu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

//explained in images section. this way we return a usable URI by the React frontend, so we can send and retrieve images.
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // WebMvcConfigurer serves to customize Spring MVC (that manages http requests) without changing the whole configuration
    private final String consoleImagesDirectory;

    // spring looks for the property, the path of where we have the images. Works for every user, as we create it in the user's system
    public WebConfig(@Value("${app.storage.console-images}")
            String consoleImagesDirectory
    ) {
        this.consoleImagesDirectory = consoleImagesDirectory;
    }

    // Normalize and change the path so Spring can use it
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(consoleImagesDirectory)
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();

        // map from an URL to a physical folder
        registry
                .addResourceHandler("/uploads/consoles/**")
                .addResourceLocations(location);
    }
}