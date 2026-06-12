package org.controllers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "org.controllers.repository.jpa")
@EnableElasticsearchRepositories(basePackages = "org.controllers.repository.elasticsearch")
public class DocSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(DocSearchApplication.class, args);
    }
}