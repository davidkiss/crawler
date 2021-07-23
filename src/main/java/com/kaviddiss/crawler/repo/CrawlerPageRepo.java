package com.kaviddiss.crawler.repo;

import com.kaviddiss.crawler.model.CrawlerPageStats;
import com.kaviddiss.crawler.domain.CrawlerPageEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CrawlerPageRepo extends CrudRepository<CrawlerPageEntity, Long> {
    @Query("select new com.kaviddiss.crawler.model.CrawlerPageStats(cp.status, count(cp.id)) from CrawlerPageEntity cp join cp.crawlerJob cj where cj.id = ?1 group by status")
    List<CrawlerPageStats> getCrawlerPageStatsByJobId(long jobId);
    @Query("from CrawlerPageEntity cp join cp.crawlerJob cj where cj.id = ?1")
    List<CrawlerPageEntity> findAllByJobId(long jobId);
    @Query("from CrawlerPageEntity cp join cp.crawlerJob cj where cj.id = ?1 and cp.url = ?2")
    List<CrawlerPageEntity> findByJobIdAndUrl(long jobId, String url);
}
