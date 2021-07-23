package com.kaviddiss.crawler.domain;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "CRAWLER_JOB")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrawlerJobEntity extends AuditableEntity {
    @Column(length = 1024)
    private String baseUrl;
}
