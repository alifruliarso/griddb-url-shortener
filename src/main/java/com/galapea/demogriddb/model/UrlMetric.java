package com.galapea.demogriddb.model;

import java.util.Date;
import com.toshiba.mwcloud.gs.RowKey;

public record UrlMetric(@RowKey Date timestamp, String shortUrl) {

    public UrlMetric(Date timestamp, String shortUrl) {
        this.timestamp = timestamp;
        this.shortUrl = shortUrl;
    }

}
