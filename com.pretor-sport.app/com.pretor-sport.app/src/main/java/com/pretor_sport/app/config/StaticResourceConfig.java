package com.pretor_sport.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.images.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //la ruta del directorio de subidas de forma absoluta
        String sourcePath = Paths.get(uploadDir).toAbsolutePath().toString() + "/";

        //mapela la url publica a la carpeta fisica
        // /images/** -> file:/ruta/absoluta/a/tu/proyecto/uploads/images/
        registry.addResourceHandler("/images/**").addResourceLocations(sourcePath);
    }
}
