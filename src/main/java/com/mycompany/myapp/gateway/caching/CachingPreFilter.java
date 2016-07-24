package com.mycompany.myapp.gateway.caching;

import com.mycompany.myapp.config.JHipsterProperties;
import com.netflix.zuul.context.RequestContext;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.netflix.zuul.constants.ZuulHeaders.ACCEPT_ENCODING;

/**
 * Use response in cache.
 */
public class CachingPreFilter extends CachingBaseFilter {

    public CachingPreFilter(CacheManager cacheManager, JHipsterProperties jHipsterProperties) {
        super(cacheManager, jHipsterProperties);
    }

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 15;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest req = ctx.getRequest();

        Cache cache = cache(ctx);
        if (cache != null) {
            String key = cacheKey(req);
            Cache.ValueWrapper valueWrapper = cache.get(key);
            if (valueWrapper != null) {
                // TODO cache should probably not store HttpServletResponse
                HttpServletResponse res = (HttpServletResponse) valueWrapper.get();
                if (res != null) {
                    log.debug("Filling response for '{}' from '{}' cache", key, cache.getName());
                    ctx.setResponse(res);
                    ctx.set(CACHE_HIT, true);
                    return res;
                }
            }
        }
        ctx.set(CACHE_HIT, false);
        return null;
    }

    private boolean isGzipRequested(RequestContext ctx) {
        String requestEncoding = ctx.getRequest().getHeader(ACCEPT_ENCODING);
        return requestEncoding != null && requestEncoding.equals("gzip");
    }

}
