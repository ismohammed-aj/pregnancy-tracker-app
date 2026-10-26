package com.pfe.mamacareplus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    @Value("${mamacareplus.videos.path}")
    private String videosPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = Path.of("uploads").toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);

        String videoPath = Path.of(videosPath).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/videos/**")
                .addResourceLocations(videoPath);

        registry.addResourceHandler("/article-images/**")
                .addResourceLocations("classpath:/static/article-images/");

        registry.addResourceHandler("/baby-development-images/**")
                .addResourceLocations("classpath:/static/baby-development-images/");
    }
}
