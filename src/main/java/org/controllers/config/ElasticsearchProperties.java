package org.controllers.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
@Getter
@Setter
public class ElasticsearchProperties
{
    private Index index = new Index();

    private Search search = new Search();

    @Getter
    @Setter
    public static class Index {
        private String name = "documents";
        private int numberOfShards = 1;
        private int numberOfReplicas = 0;
    }

    @Getter
    @Setter
    public static class Search {
        private int defaultSize = 10;
        private int maxSize = 100;
        private int highlightFragmentSize = 150;
        private int highlightNumberOfFragments = 3;
    }
}
