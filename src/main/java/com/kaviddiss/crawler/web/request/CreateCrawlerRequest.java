package com.kaviddiss.crawler.web.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class CreateCrawlerRequest {
    @NotNull
    @Size(min = 5, max = 1024)
    private String baseUrl;
}
