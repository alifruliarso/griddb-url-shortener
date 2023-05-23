package com.galapea.demogriddb.service;

import static com.galapea.demogriddb.config.ShortUrlMapper.SHORT_URL_MAPPER_INSTANCE;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.galapea.demogriddb.exception.UrlShortenerException;
import com.galapea.demogriddb.model.ShortUrlResponse;
import com.galapea.demogriddb.model.UrlMetricModel;
import com.galapea.demogriddb.model.UrlModel;
import com.galapea.demogriddb.model.UrlPayload;
import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.RowSet;
import com.toshiba.mwcloud.gs.TimeSeries;
import io.hypersistence.tsid.TSID;

@Service
public class UrlService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlService.class);
    private final BaseConversion conversion;
    private final Collection<Long, UrlModel> urlCollection;
    private final TimeSeries<UrlMetricModel> urlMetricContainer;

    public UrlService(BaseConversion conversion, Collection<Long, UrlModel> urlCollection,
            TimeSeries<UrlMetricModel> urlMetricContainer) {
        this.conversion = conversion;
        this.urlCollection = urlCollection;
        this.urlMetricContainer = urlMetricContainer;
    }

    public static long nextId() {
        return TSID.Factory.getTsid().toLong();
    }

    public ShortUrlResponse create(UrlPayload payload) {
        UrlModel model = new UrlModel();
        model.setId(nextId());
        model.setOriginalUrl(payload.getUrl());
        model.setShortUrl(generateShortUrl(model.getId()));
        LOGGER.info("Encode {}", model);
        try {
            urlCollection.setAutoCommit(false);
            urlCollection.put(model.getId(), model);
            urlCollection.commit();
        } catch (GSException e) {
            e.printStackTrace();
            throw new UrlShortenerException("Please try again");
        }
        return SHORT_URL_MAPPER_INSTANCE.mapEntityToResponse(model);
    }

    private String generateShortUrl(Long id) {
        String uniqueId = conversion.encode(id);
        return uniqueId.substring(0, 7);
    }

    public ShortUrlResponse findOriginalUrl(String shortUrl) {
        Query<UrlModel> query;
        try {
            urlCollection.setAutoCommit(false);
            query = urlCollection.query("select * where shortUrl = '" + shortUrl + "'");
            RowSet<UrlModel> rs = query.fetch(true);
            if (!rs.hasNext()) {
                throw new UrlShortenerException(String.format("%s not found", shortUrl));
            }
            while (rs.hasNext()) {
                UrlModel model = rs.next();
                model.setClicks(model.getClicks() + 1);
                rs.update(model);
                urlCollection.commit();
                return SHORT_URL_MAPPER_INSTANCE.mapEntityToResponse(model);
            }
        } catch (GSException e) {
            e.printStackTrace();
            throw new UrlShortenerException("Please try again");
        }
        throw new UrlShortenerException("URL not found");
    }

    public void saveMetric(String shortUrl, String userAgent, String ipAddress) {
        UrlMetricModel metric = new UrlMetricModel();
        metric.setShortUrl(shortUrl);
        metric.setUserAgent(userAgent);
        metric.setIpAddress(ipAddress);
        try {
            urlMetricContainer.append(metric);
        } catch (GSException e) {
            e.printStackTrace();
        }
    }

    public List<UrlModel> getUrls() {
        List<UrlModel> urls = new ArrayList<>();
        Query<UrlModel> query;
        try {
            query = urlCollection.query("select * from urls");
            RowSet<UrlModel> rs = query.fetch();
            while (rs.hasNext()) {
                UrlModel model = rs.next();
                urls.add(model);
            }
        } catch (GSException e) {
            e.printStackTrace();
        }
        return urls;
    }

    public List<UrlMetricModel> getMetrics() {
        List<UrlMetricModel> metricModels = new ArrayList<>();
        Query<UrlMetricModel> query;
        try {
            query = urlMetricContainer.query("select * from urlmetrics");
            RowSet<UrlMetricModel> rs = query.fetch();
            while (rs.hasNext()) {
                UrlMetricModel model = rs.next();
                metricModels.add(model);
            }
        } catch (GSException e) {
            e.printStackTrace();
        }
        return metricModels;
    }
}
