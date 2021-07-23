package com.kaviddiss.crawler.domain;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "CRAWLER_PAGE", uniqueConstraints = { @UniqueConstraint(columnNames = { "crawler_job_id", "url" }) })
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrawlerPageEntity extends AuditableEntity {
    public enum Status { QUEUED, CRAWLED, FAILED }

    private String url;
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    private CrawlerJobEntity crawlerJob;
    @ElementCollection
    private List<String> assets;
    @ElementCollection
    private List<String> referredPages;
}
