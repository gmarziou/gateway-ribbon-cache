package com.mycompany.myapp.gateway.caching;

import com.mycompany.myapp.config.JHipsterProperties;
import com.netflix.zuul.context.RequestContext;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Store response in cache.
 * There is a cache per service.
 */
public class CachingPostFilter extends CachingBaseFilter {

    public CachingPostFilter(CacheManager cacheManager, JHipsterProperties jHipsterProperties) {
        super(cacheManager, jHipsterProperties);
    }

    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 900;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        return ctx.get(CACHE_HIT) == null;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest req = ctx.getRequest();
        HttpServletResponse res = ctx.getResponse();

        if (res.getStatus() < 300) {
            // Store only successful responses
            Cache cache = cache(ctx);
            if (cache != null) {
                String key = cacheKey(req);
                cache.put(key, res);
                log.debug("Cached successful response for '{}' into '{}' cache", key, cache.getName());
            }
        }
        return null;
    }

}
