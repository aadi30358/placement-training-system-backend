package com.example.pts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@EnableAsync
@OpenAPIDefinition(info = @Info(title = "PTS API", version = "1.0", description = "Placement Training System API v1.0"))
public class PtsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PtsBackendApplication.class, args);
    }

}
