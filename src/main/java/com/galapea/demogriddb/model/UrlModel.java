package com.galapea.demogriddb.model;

import com.toshiba.mwcloud.gs.RowKey;
import lombok.Data;

@Data
public class UrlModel {
    @RowKey
    Long id;
    String shortUrl;
    String originalUrl;
    int clicks;
}
