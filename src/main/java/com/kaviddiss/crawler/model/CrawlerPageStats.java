package com.kaviddiss.crawler.model;

import com.kaviddiss.crawler.domain.CrawlerPageEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CrawlerPageStats {
    private CrawlerPageEntity.Status status;
    private long count;
}
