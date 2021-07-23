package com.kaviddiss.crawler.config;

import com.kaviddiss.crawler.service.CrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class StreamConfig {
    @Bean
    public Consumer<Long> crawlPage(CrawlerService crawlerService) {
        return pageId -> crawlerService.crawlPage(pageId)
                .forEach(crawlerService::queuePageForCrawling);
    }
}
