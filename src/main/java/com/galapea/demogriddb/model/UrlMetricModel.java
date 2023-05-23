package com.galapea.demogriddb.model;

import java.util.Date;
import com.toshiba.mwcloud.gs.RowKey;
import lombok.Data;

@Data
public class UrlMetricModel {
    @RowKey
    Date timestamp;
    String shortUrl;
    String userAgent;
    String ipAddress;
}
