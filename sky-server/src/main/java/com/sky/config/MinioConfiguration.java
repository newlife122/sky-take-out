package com.sky.config;

import com.sky.properties.MinioOssProperties;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author raoxin
 */
@Configuration
public class MinioConfiguration {

    @Autowired
    private MinioOssProperties properties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKeyId(),properties.getAccessKeySecret())
                .build();
    }
}