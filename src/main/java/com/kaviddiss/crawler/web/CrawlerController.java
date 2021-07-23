package com.kaviddiss.crawler.web;

import com.kaviddiss.crawler.domain.CrawlerJobEntity;
import com.kaviddiss.crawler.model.CrawlerJobStats;
import com.kaviddiss.crawler.model.CrawlerPageDto;
import com.kaviddiss.crawler.service.CrawlerService;
import com.kaviddiss.crawler.web.request.CreateCrawlerRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/crawlers", produces = MediaType.APPLICATION_JSON_VALUE)
public class CrawlerController {
    private final CrawlerService crawlerService;

    @PostMapping
    public CrawlerJobEntity createCrawlerJob(@RequestBody @Valid CreateCrawlerRequest request) {
        return crawlerService.createCrawlerJob(request.getBaseUrl());
    }

    @GetMapping("{jobId}/stats")
    public CrawlerJobStats getJobStats(@PathVariable long jobId) {
        return crawlerService.getJobStats(jobId);
    }

    @GetMapping("{jobId}/pages")
    public List<CrawlerPageDto> getJobPages(@PathVariable long jobId) {
        return crawlerService.getPages(jobId);
    }
}
