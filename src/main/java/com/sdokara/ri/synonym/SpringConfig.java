package com.sdokara.ri.synonym;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.stream.Stream;

import static org.springframework.http.HttpMethod.*;

@Configuration
public class SpringConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods(Stream.of(HEAD, GET, POST, PUT, PATCH, DELETE)
                                .map(HttpMethod::name).toArray(String[]::new));
            }
        };
    }
}
