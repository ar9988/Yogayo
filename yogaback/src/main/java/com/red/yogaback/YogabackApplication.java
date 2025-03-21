package com.red.yogaback;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@OpenAPIDefinition(servers = {@Server(url = "https://j12d104.p.ssafy.io",description = "https"), @Server(url = "http://localhost:8080",description = "로컬")})
public class YogabackApplication {

    public static void main(String[] args) {
        SpringApplication.run(YogabackApplication.class, args);
    }

}
