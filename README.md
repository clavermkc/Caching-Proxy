# Caching Proxy

> A work-in-progress caching proxy server built with Java 21 and Spring Boot.

This project is my implementation of the [Caching Server project on roadmap.sh](https://roadmap.sh/projects/caching-server). It forwards `GET` requests to an origin server, stores each response in an in-memory cache, and returns cached responses for repeated requests.

[Deutsche Version](README_DE.md)

## Current status

The proxy and its in-memory cache are implemented. The Roadmap CLI interface and cache-clearing command are **not implemented yet**, so this project is not yet a complete solution to the exercise.

Implemented:

- Forward `GET` requests to a configurable origin server.
- Cache responses by request path and query string in a thread-safe in-memory map.
- Preserve the origin response headers and body.
- Add `X-Cache: MISS` when the origin server was called and `X-Cache: HIT` when the response came from cache.
- Unit-test the cache-hit path.

Not implemented yet:

- A `caching-proxy --port <number> --origin <url>` CLI command.
- A `caching-proxy --clear-cache` CLI command.
- Cache expiry/eviction. The cache currently remains populated until the application stops.
- Proxying HTTP methods other than `GET`.

## Prerequisites

- Java 21
- No separate Maven installation is needed: the Maven Wrapper is included.

## Run locally

The current application uses Spring Boot configuration rather than the final CLI. The default configuration is:

```properties
server.port=8082
proxy.origin=https://dummyjson.com
```

Start it with the defaults:

```bash
./mvnw spring-boot:run
```

Or build the JAR and supply a different port and origin through Spring Boot arguments:

```bash
./mvnw package
java -jar target/Caching_Proxy-0.0.1-SNAPSHOT.jar --server.port=3000 --proxy.origin=http://dummyjson.com
```

Then make the same request twice:

```bash
curl -i http://localhost:3000/products
curl -i http://localhost:3000/products
```

The first response contains `X-Cache: MISS`; the second contains `X-Cache: HIT`.

## How it works

1. The controller receives a `GET` request and keeps its path and optional query string.
2. The service looks up that value in `CacheRepository`.
3. On a miss, it calls `proxy.origin + path`, returns the origin response, and saves the body and headers in the cache.
4. On a hit, it returns the stored body and headers without calling the origin server.

## Demonstration

The first request goes to the origin server (about 1.8 s in this capture):

![First request served by the origin server](docs/images/origin-request-cache-miss.png)

The repeated request is served much faster from cache (34 ms in this capture):

![Repeated request served from cache](docs/images/cached-request-cache-hit.png)

The application logs confirm the expected sequence: `X-Cache: MISS`, followed by `X-Cache: HIT`.

![Application logs showing a cache miss followed by a cache hit](docs/images/cache-hit-miss-logs.png)

## Tests

```bash
./mvnw test
```

## Next step

Implement the actual `caching-proxy` command-line interface, mapping `--port` and `--origin` to the Spring Boot configuration, and add `--clear-cache` so the cache can be cleared without changing code. After that, add integration tests that verify a real miss followed by a hit and test cache clearing.

## License

No license has been selected yet.
