Building URL Shortener API using Java Spring-Boot & GridDB
===
Are you tired of long and cumbersome URLs? Do you find it challenging to share them with your friends and colleagues? If yes, then you are not alone. Long URLs can be frustrating and challenging to manage. Fortunately, there is a solution to this problem - URL shorteners. In this article, we will discuss how to build a simple URL shortener using Java Spring-Boot and GridDB.

[toc]


## What is URL Shortener?

Url Shortener is a service that creates a short link from a very long URL. Clicking on a sort URL, the user will be redirected to the original URL.
For example, the long URL https://developers.googleblog.com/2018/03/transitioning-google-url-shortener.html can be shortened to **goo.gl**. The shortened URL is easier to share, and it takes up less space.

## Why Build a URL Shortener?

There are several reasons why you might want to build a URL shortener.
1. It can help you manage long URLs more efficiently.
2. It can help you track clicks and user engagement.
3. It can help you brand your URLs. For example, you can use your brand name in the shortened URL.

## Features
Before we dive into the technical details, let's take a look at some of the features that we want to implement in our URL shortener service:
1. The service should be REST API accessible
2. Given a long URL should generate a unique Short URL
3. Given a short URL should redirect to the original URL
4. Service should provide analytics features (most visited links, how many times the URL was visited)


## System Design
The most important thing to keep in mind here is that our system will be **read-heady**. The number of reading requests will be 1000 times more than the number of write requests.

### Encoding actual URL
* Compute a unique ID of the given URL. In this tutorial I use TSID.
* Use base62 ecoding ([A-Z, a-z, 0–9]) to encode ID into unique string. We take the first 7 characters as the short URL.

## SQL or NoSQL Database
One of the key decisions that we need to make when building our URL shortener service is which database to use. There are two main types of databases: SQL and NoSQL databases.

SQL databases are relational databases that store data in tables with predefined schemas. They are good for handling structured data and transactions.

NoSQL databases, on the other hand, are non-relational databases that store data in a flexible, schema-less format. They are good for handling unstructured data and scaling horizontally.

For our URL shortener service, we will be using a NoSQL database. It is a good fit for our use case because we don't really need a relationship among data, but we need fast read and write speed.

In this tutorial we will use GridDB: a highly scalable in memory, NoSQL time-series database optimized for IoT and big data applications. 

The Key Container model allows high-speed access to data through Java and C APIs. Data in GridDB is also queried through TQL, a custom SQL-like query language. Basic search through the WHERE command and high-speed conditional search operations through indexing offers a great advantage for applications that rely on faster search. GridDB supports transactions, including those with plural records from the application. Transactions in GridDB guarantee ACID (Atomicity, Consistency, Isolation, and Durability) at the container level.

In this tutorial, we just need 2 tables, one for storing URLs and the other for storing metrics.

## Setting up Java application
In this tutorial, we will be using the following stack:
* Java OpenJDK 17
* Docker 23.0.1
* Spring Boot 3.0.5
* apache-maven-3.8.7

### GridDB Instance
Before creating the API endpoint, we need to acquire a connection between the application and the cluster database of GridDB. Here we create an instance of GridStore class.
```java
    @Bean
    public GridStore gridStore() throws GSException {
        // Acquiring a GridStore instance
        Properties properties = new Properties();
        properties.setProperty("notificationMember", "griddbserver:10001");
        properties.setProperty("clusterName", "dockerGridDB");
        properties.setProperty("user", "admin");
        properties.setProperty("password", "admin");
        GridStore store = GridStoreFactory.getInstance().getGridStore(properties);
        return store;
    }
```

### Create dataset for storing URLs
We set the Id column as primary key.
```java
    @Data
    public class UrlModel {
        @RowKey
        Long id;
        String shortUrl;
        String originalUrl;
        int clicks;
    }
```

### Create dataset for storing metrics of URLs
For timeseries data we need a primary key in timestamp format.
```java
    @Data
    public class UrlMetricModel {
        @RowKey
        Date timestamp;
        String shortUrl;
        String userAgent;
        String ipAddress;
    }
```

### Create a Container for managing the Rows
After setting up the DB connection, we can use it to create the container. We are using the Collection container to store URLs. We are using the **shortUrl** field as an index because this field will be used in the condition of the WHERE query.
```java
    @Bean
    public Collection<Long, UrlModel> urlCollection(GridStore gridStore) throws GSException {
        Collection<Long, UrlModel> urlCollection = gridStore.putCollection("urls", UrlModel.class);
        urlCollection.createIndex("shortUrl");
        return urlCollection;
    }
```

For the URL metrics, we are using the TimeSeries container because we need to manage rows with the occurrence time.
```java
    @Bean
    public TimeSeries<UrlMetricModel> urlMetricContainer(GridStore gridStore) throws GSException {
        return gridStore.putTimeSeries("urlmetrics", UrlMetricModel.class);
    }
```

## REST API Endpoints

We will create the following endpoints for our application:
1. POST `/api/urls`. Create a new short URL resource.

    ```java
    public ShortUrlResponse create(UrlPayload payload) {
        UrlModel model = new UrlModel();
        model.setId(nextId());
        model.setOriginalUrl(payload.getUrl());
        model.setShortUrl(generateShortUrl(model.getId()));
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
    ```
    We are using the Java API to put the URL data into GridDB. 
    
    **Payload example**:
 
    ```json
    {
      "url": "https://www.javacodegeeks.com/2023/05/what-are-events-relation-to-api-calls.html"
    }
    ```
  
    **Success Response**:
    ```json
    {
      "url": "https://www.javacodegeeks.com/2023/05/what-are-events-relation-to-api-calls.html",
      "shortUrl": "HjlQjET"
    }
    ```
    This endpoint will return a JSON value containing the short URL generated by the service class, or throw an exception in case an error occurred.

2. GET `/api/urls/{shorUrl}`. Find the original URL by short URL.
    ```java
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
    ```
    In the first part, we try to query the URL by shortUrl. If the row is not found, we throw an exception. If we found the row, here we need to increment the count of clicks. After that, we are committing the transaction.
    
    **Success Response**:
    ```json
    {
      "url": "https://www.javacodegeeks.com/2023/05/what-are-events-relation-to-api-calls.html",
      "shortUrl": "HjlQjET"
    }
    ```

3. GET `/api/urls`. List all URLs with the information about how many times the URL has been visited
    ```java
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
    ```
    This code is for fetching all the URLs in the collection.
    
    **Success Response**:
    ```json
    {    
        "data":[
            {
              "id": 435224999694707071
              "shortUrl": "GjuWlGRgHH",
              "originalUrl": "https://developers.googleblog.com/2018/03/transitioning-google-url-shortener.html"
              "clicks": 7
            }
        ]
    }
    ```

4. GET `/api/metrics`. List all url metrics
  
    **Success Response**:
    ```json
    {
      "data":[
        {
          "timestamp": "2023-05-21T18:56:08.656+00:00",
          "shortUrl": "HgUsO2Cj7d",
          "userAgent": "vscode-restclient",
          "ipAddress": "172.23.0.1"
        }
      ]
    }
    ```

## Scaling the API
These items are the things that must be considered for making a production-ready system:
* High availability: the API should be highly available. URL redirection and response time should happen in real-time with minimal latency.
* Caching for improved latency: we can improve the response time of our API by caching frequently accessed short ULRs or the top 10% of daily searches.
* Load balancing: determines which server is available to handle which request. The load balancer also serves as a single point of contact for all of our users.
* Data capacity: to understand how much data we might have to insert into our system, and calculate the storage of data for 5 or 10 years.
* Security: the API should be designed to prevent malicious users from generating short links to phishing or malware sites, and protect against DDoS attacks and brute force attacks.


## Conclusion
In conclusion, building a simple URL shortener using Java Spring-Boot and GridDB is a straightforward process. By following the steps outlined in this article, you can create a URL shortener that is easy to manage and track.
