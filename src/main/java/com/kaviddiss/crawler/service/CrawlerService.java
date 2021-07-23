package com.kaviddiss.crawler.service;

import com.kaviddiss.crawler.domain.CrawlerJobEntity;
import com.kaviddiss.crawler.domain.CrawlerPageEntity;
import com.kaviddiss.crawler.model.CrawlerJobStats;
import com.kaviddiss.crawler.model.CrawlerPageDto;
import com.kaviddiss.crawler.model.CrawlerPageStats;
import com.kaviddiss.crawler.repo.CrawlerJobRepo;
import com.kaviddiss.crawler.repo.CrawlerPageRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CrawlerService {
    private final CrawlerJobRepo crawlerJobRepo;
    private final CrawlerPageRepo crawlerPageRepo;
    private final StreamBridge streamBridge;

    public CrawlerJobEntity createCrawlerJob(String baseUrl) {
        CrawlerJobEntity crawlerJob = crawlerJobRepo.save(CrawlerJobEntity.builder()
                .baseUrl(baseUrl)
                .build());
        CrawlerPageEntity crawlerPage = crawlerPageRepo.save(CrawlerPageEntity.builder()
                .url(crawlerJob.getBaseUrl())
                .status(CrawlerPageEntity.Status.QUEUED)
                .crawlerJob(crawlerJob)
                .build());

        queuePageForCrawling(crawlerPage);
        return crawlerJob;
    }

    private void queuePageForCrawling(CrawlerPageEntity crawlerPage) {
        queuePageForCrawling(crawlerPage.getId());
    }

    public void queuePageForCrawling(long crawlerPaged) {
        streamBridge.send("crawlPage-out", crawlerPaged);
    }

    @Transactional(readOnly = true)
    public CrawlerJobStats getJobStats(long jobId) {
        List<CrawlerPageStats> pageStatsByJobId = crawlerPageRepo.getCrawlerPageStatsByJobId(jobId);
        long crawledCount = 0;
        long queuedCount = 0;
        long failedCount = 0;
        for (CrawlerPageStats pageStats : pageStatsByJobId) {
            switch (pageStats.getStatus()) {
                case CRAWLED:
                    crawledCount = pageStats.getCount();
                    break;
                case QUEUED:
                    queuedCount = pageStats.getCount();
                    break;
                case FAILED:
                    failedCount = pageStats.getCount();
                    break;
            }
        }
        return new CrawlerJobStats(jobId, crawledCount, queuedCount, failedCount);
    }

    @Transactional(readOnly = true)
    public List<CrawlerPageDto> getPages(long jobId) {
        return crawlerPageRepo.findAllByJobId(jobId).stream()
                .map(this::crawlerPageToDto)
                .collect(Collectors.toList());
    }

    private CrawlerPageDto crawlerPageToDto(CrawlerPageEntity crawlerPage) {
        return CrawlerPageDto.builder()
                .url(crawlerPage.getUrl())
                .status(crawlerPage.getStatus())
                .assets(new TreeSet<>(crawlerPage.getAssets()))
                .referredPages(new TreeSet<>(crawlerPage.getReferredPages()))
                .build();
    }

    /**
     * Parses page to find assets and links to other pages
     * @param pageId id of CrawlerPageEntity
     * @return list of linkIds of other pages found on this page
     */
    public Stream<Long> crawlPage(long pageId) {
        Optional<CrawlerPageEntity> maybeCrawlerPage = crawlerPageRepo.findById(pageId);
        if (maybeCrawlerPage.isEmpty()) {
            log.error("No page found with id {}", pageId);
            return Stream.empty();
        }

        List<CrawlerPageEntity> pages = new ArrayList<>();
        CrawlerPageEntity crawlerPage = maybeCrawlerPage.get();
        log.info("Crawling page {}", crawlerPage.getUrl());
        try {
            Document doc = Jsoup.connect(crawlerPage.getUrl()).get();
            List<String> assets = parseAssets(crawlerPage, doc);
            crawlerPage.setAssets(assets);

            List<String> referredPages = parseElements(doc, crawlerPage, "a", "href");
            crawlerPage.setReferredPages(referredPages);
            pages.addAll(referredPages.stream()
                    .map(url -> urlToCrawlerPage(url, crawlerPage.getCrawlerJob()))
                    .collect(Collectors.toList()));

            crawlerPage.setStatus(CrawlerPageEntity.Status.CRAWLED);
            log.info("Parsing page completed for {} with id {}", crawlerPage.getUrl(), crawlerPage.getId());
        } catch (IOException e) {
            log.error("Failed to parse page {} with id {}", crawlerPage.getUrl(), crawlerPage.getId(), e);
            crawlerPage.setStatus(CrawlerPageEntity.Status.FAILED);
        }
        return pages.stream()
                .filter(this::isUrlAlreadyQueued)
                .map(crawlerPageRepo::save)
                .map(CrawlerPageEntity::getId);
    }

    private static CrawlerPageEntity urlToCrawlerPage(String url, CrawlerJobEntity crawlerJob) {
        return CrawlerPageEntity.builder()
                .url(url)
                .status(CrawlerPageEntity.Status.QUEUED)
                .crawlerJob(crawlerJob)
                .build();
    }

    private boolean isUrlAlreadyQueued(CrawlerPageEntity crawlerPage) {
        return crawlerPageRepo.findByJobIdAndUrl(crawlerPage.getCrawlerJob().getId(), crawlerPage.getUrl()).isEmpty();
    }

    @NotNull
    private List<String> parseAssets(CrawlerPageEntity crawlerPage, Document doc) {
        log.info("Parsing page {} at {}", doc.title(), crawlerPage.getUrl());
        List<String> assets = new ArrayList<>();
        assets.addAll(parseElementsWithCssQuery(doc, crawlerPage, "link[rel=\"stylesheet\"]", "href"));
        assets.addAll(parseElements(doc, crawlerPage, "script", "src"));
        assets.addAll(parseElements(doc, crawlerPage, "img", "src"));
        assets.addAll(parseElements(doc, crawlerPage, "object", "data"));
        return assets;
    }

    private List<String> parseElements(Document htmlDoc,
                                       CrawlerPageEntity crawlerPage,
                                       String elementName,
                                       String urlAttributeName) {
        String cssQuery = String.format("%s[%s]", elementName, urlAttributeName);
        return parseElementsWithCssQuery(htmlDoc, crawlerPage, cssQuery, urlAttributeName);
    }

    private List<String> parseElementsWithCssQuery(Document htmlDoc,
                                                   CrawlerPageEntity crawlerPage,
                                                   String cssQuery, String urlAttributeName) {
        List<String> links = htmlDoc.select(cssQuery).stream()
                .map(element -> element.absUrl(urlAttributeName))
                .filter(link -> link.startsWith(crawlerPage.getCrawlerJob().getBaseUrl()))
                .collect(Collectors.toList());
        log.info("Found {} link(s) with css query {} at {}", links.size(), cssQuery, crawlerPage.getUrl());
        return links;
    }
}
