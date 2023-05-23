package com.galapea.demogriddb.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.galapea.demogriddb.HttpRequestResponseUtils;
import com.galapea.demogriddb.exception.NotFoundException;
import com.galapea.demogriddb.model.ShortUrlResponse;
import com.galapea.demogriddb.model.UrlMetricModel;
import com.galapea.demogriddb.model.UrlModel;
import com.galapea.demogriddb.model.UrlPayload;
import com.galapea.demogriddb.service.UrlService;

@RestController
@RequestMapping("/api/urls")
public class UrlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlController.class);
    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @GetMapping()
    public ResponseEntity<?> getAll() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        List<UrlModel> urlList = urlService.getUrls();
        if (!urlList.isEmpty()) {
            map.put("status", 1);
            map.put("data", urlList);
            return new ResponseEntity<>(map, HttpStatus.OK);
        } else {
            map.clear();
            map.put("status", 0);
            map.put("message", "Data is not found");
            return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{url}")
    public ResponseEntity<ShortUrlResponse> getOriginalUrl(
            @RequestHeader(value = "User-Agent") String userAgent,
            @PathVariable("url") String shortUrl) throws NotFoundException {
        LOGGER.info("Received shortened url to redirect: " + shortUrl);
        var response = urlService.findOriginalUrl(shortUrl);
        LOGGER.info("Original URL: " + response.getUrl());
        urlService.saveMetric(shortUrl, userAgent, HttpRequestResponseUtils.getClientIpAddress());
        return ResponseEntity.ok().body(response);
    }

    @PostMapping()
    public ResponseEntity<ShortUrlResponse> create(@RequestBody UrlPayload model) {
        LOGGER.info("Received url to shorten: " + model.getUrl());
        var response = urlService.create(model);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping(value = "/metrics")
    public ResponseEntity<?> getMetrics() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        List<UrlMetricModel> urlList = urlService.getMetrics();
        if (!urlList.isEmpty()) {
            map.put("status", 1);
            map.put("data", urlList);
            return new ResponseEntity<>(map, HttpStatus.OK);
        } else {
            map.clear();
            map.put("status", 0);
            map.put("message", "Data is not found");
            return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
        }
    }

}
