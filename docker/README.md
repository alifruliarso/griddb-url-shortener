## build image
```
docker build -t local/griddb-server .
```

## run container
docker run --network griddb-net -p 10001:10001 --name griddb-server -v griddb_data:/var/lib/gridstore -t local/griddb-server

## Create a network
docker network create griddb-net