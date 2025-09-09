package Commune.Dev.Config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${app.photo.upload.dir:uploads/photos/marchands}")
    private String photoUploadDirectory;

    @Value("${app.photo.access.url:/photos/marchands/**}")
    private String photoAccessUrl;

    @PostConstruct
    public void createUploadDirectory() {
        try {
            Path path = Paths.get(photoUploadDirectory);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                System.out.println("✅ Dossier créé pour les photos : " + path.toAbsolutePath());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création du dossier d'upload : " + e.getMessage(), e);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map l'URL pour accéder aux photos uploadées
        registry.addResourceHandler(photoAccessUrl)
                .addResourceLocations("file:" + photoUploadDirectory + File.separator);
    }
}
