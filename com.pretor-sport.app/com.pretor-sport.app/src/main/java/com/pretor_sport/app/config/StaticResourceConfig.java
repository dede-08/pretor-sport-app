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
        try {
            //la ruta del directorio de subidas de forma absoluta
            String absolutePath = Paths.get(uploadDir).toAbsolutePath().toString();
            
            //verificar que el directorio existe
            java.io.File uploadDirectory = new java.io.File(absolutePath);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
            }
            
            //mapear la url publica a la carpeta fisica
            // /images/** -> file:/ruta/absoluta/a/tu/proyecto/uploads/
            registry.addResourceHandler("/images/**")
                    .addResourceLocations("file:" + absolutePath + "/");
        } catch (Exception e) {
            //loggea un error pero no interrumpe el inicio
            System.err.println("Error configuring resource handlers: " + e.getMessage());
        }
    }
}
