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
        return super.shouldFilter() && !ctx.getBoolean(CACHE_HIT);
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest req = ctx.getRequest();
        HttpServletResponse res = ctx.getResponse();

        if (isSuccess(res)) {
            // Store only successful responses
            Cache cache = cache(ctx);
            if (cache != null) {
                // TODO cache should probably not store HttpServletResponse
                String key = cacheKey(req);
                cache.put(key, res);
                log.debug("Cached successful response for '{}' into '{}' cache", key, cache.getName());
            }
        }
        return null;
    }

    private boolean isSuccess(HttpServletResponse res) {
        return (res != null) && (res.getStatus() < 300);
    }

}
