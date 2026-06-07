package org.controllers.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "storage")
@Getter
@Setter
public class StorageConfig {

    private String uploadDir = "./uploads";

    private List<String> allowedExtensions;

    private long maxFileSize = 50 * 1024 * 1024;
}
