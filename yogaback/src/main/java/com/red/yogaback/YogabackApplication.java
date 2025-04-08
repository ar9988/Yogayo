package com.red.yogaback;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
//@EnableAsync
@OpenAPIDefinition(servers = {@Server(url = "https://j12d104.p.ssafy.io",description = "https"), @Server(url = "http://localhost:8080",description = "로컬")})
public class YogabackApplication {

    public static void main(String[] args) {
        SpringApplication.run(YogabackApplication.class, args);
    }

}
