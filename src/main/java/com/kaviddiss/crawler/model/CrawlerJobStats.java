package com.kaviddiss.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CrawlerJobStats {
    private long jobId;
    private long crawledPages;
    private long queuedPages;
    private long failedPages;
}
