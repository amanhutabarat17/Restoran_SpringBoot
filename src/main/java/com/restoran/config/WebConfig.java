package com.restoran.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Memetakan folder fisik tempat file diupload (lihat UploadController) supaya
 * bisa diakses lewat browser via URL /uploads/... (mis. /uploads/menu/xxx.jpg).
 * Tanpa ini, file tersimpan di disk tapi tidak bisa ditampilkan di <img>.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path folderUpload = Paths.get(uploadDir).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + folderUpload + "/");
    }
}