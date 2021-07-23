package com.kaviddiss.crawler.model;


import com.kaviddiss.crawler.domain.CrawlerPageEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Builder
@Getter
public class CrawlerPageDto {
    private String url;
    private CrawlerPageEntity.Status status;
    private Set<String> assets;
    private Set<String> referredPages;
}
