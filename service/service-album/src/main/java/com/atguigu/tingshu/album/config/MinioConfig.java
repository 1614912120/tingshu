package com.atguigu.tingshu.album.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Autowired
    private MinioConstantProperties properties;


    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.getEndpointUrl())
                .credentials(properties.getAccessKey(),properties.getSecreKey())
                .build();
    }
}
