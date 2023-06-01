package com.galapea.demogriddb.config;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.galapea.demogriddb.model.UrlMetricModel;
import com.galapea.demogriddb.model.UrlModel;
import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.GridStoreFactory;
import com.toshiba.mwcloud.gs.TimeSeries;

@Configuration
public class GridDBConfig {

    @Value("${GRIDDB_NOTIFICATION_MEMBER}")
    private String notificationMember;
    @Value("${GRIDDB_CLUSTER_NAME}")
    private String clusterName;
    @Value("${GRIDDB_USER}")
    private String user;
    @Value("${GRIDDB_PASSWORD}")
    private String password;

    @Bean
    public GridStore gridStore() throws GSException {
        // Acquiring a GridStore instance
        Properties properties = new Properties();
        properties.setProperty("notificationMember", notificationMember);
        properties.setProperty("clusterName", clusterName);
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        GridStore store = GridStoreFactory.getInstance().getGridStore(properties);
        return store;
    }

    @Bean
    public Collection<Long, UrlModel> urlCollection(GridStore gridStore) throws GSException {
        Collection<Long, UrlModel> urlCollection = gridStore.putCollection("urls", UrlModel.class);
        urlCollection.createIndex("shortUrl");
        return urlCollection;
    }

    @Bean
    public TimeSeries<UrlMetricModel> urlMetricContainer(GridStore gridStore) throws GSException {
        return gridStore.putTimeSeries("urlmetrics", UrlMetricModel.class);
    }

}
