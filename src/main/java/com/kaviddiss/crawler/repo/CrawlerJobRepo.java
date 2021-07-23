package com.kaviddiss.crawler.repo;

import com.kaviddiss.crawler.domain.CrawlerJobEntity;
import org.springframework.data.repository.CrudRepository;

public interface CrawlerJobRepo extends CrudRepository<CrawlerJobEntity, Long> {
}
