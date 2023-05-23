# Test java client connection

## Build java client
```
docker build -t griddb-java .
```

## Run java client, connection to local server
```
docker run --network griddb-net -e IP_NOTIFICATION_MEMBER=griddb-server -t griddb-java
```