package com.smartdoc.api;

import com.smartdoc.rag.DocumentIngestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@Configuration
public class AppConfig implements WebMvcConfigurer {

    private static final String SAMPLE_DOCS_PATH = "classpath:sample-docs/";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Bean
    public CommandLineRunner loadSampleDocuments(DocumentIngestionService ingestionService) {
        return args -> {
            try {
                Resource[] resources = new PathMatchingResourcePatternResolver()
                        .getResources(SAMPLE_DOCS_PATH + "*");

                for (Resource resource : resources) {
                    if (resource.exists() && resource.isFile()) {
                        try {
                            Path file = Path.of(resource.getURI());
                            ingestionService.ingestDocument(file);
                            log.info("Loaded sample document: {}", file.getFileName());
                        } catch (Exception e) {
                            log.warn("Failed to load sample document: {}", resource.getFilename(), e);
                        }
                    }
                }
                log.info("Sample documents loading completed");
            } catch (IOException e) {
                log.warn("Failed to access sample documents directory (this is OK if no sample docs exist)", e);
            }
        };
    }
}
