# Gateway caching

Caching implemented with 2 filters:

- CachingPreFilter: reads cache and fill response if URI found in cache
- CachingPostFilter: stores response into cache using URI as key

Cache name uses service id.

We should take into account whether the response is gzipped.
