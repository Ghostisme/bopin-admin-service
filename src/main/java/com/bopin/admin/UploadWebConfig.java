package com.bopin.admin;

import java.nio.file.Path;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadWebConfig implements WebMvcConfigurer {
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String location = Path.of("data", "uploads").toAbsolutePath().normalize().toUri().toString();
    if (!location.endsWith("/")) location += "/";
    registry.addResourceHandler("/uploads/**").addResourceLocations(location);
  }
}
