package com.mycompany.myapp.gateway.caching;

import com.mycompany.myapp.config.JHipsterProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Base class for caching filters.
 */
public abstract class CachingBaseFilter extends ZuulFilter {
    protected Logger log = LoggerFactory.getLogger(getClass());

    /**
     * This is where other filters store the id of the downstream service.
     */
    public static final String SERVICE_ID = "serviceId";

    /**
     * Stores the fact that response has been provided by cache in pre filter
     * so that post filter does not store the value in cache again.
     */
    public static final String CACHE_HIT = "cacheHit";

    protected final CacheManager cacheManager;


    public CachingBaseFilter(CacheManager cacheManager, JHipsterProperties jHipsterProperties) {
        this.cacheManager = cacheManager;
    }

    /**
     * Should filter if request is a GET and if there exists a cache for this service.
     */
    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest req = ctx.getRequest();

        if (!getVerb(req).equals("GET")) {
            return false;
        }

        String serviceId = serviceId(ctx);
        if ((serviceId != null) && (cache(ctx) != null)) {
            return true;
        }
        return false;
    }

    private String serviceId(RequestContext ctx) {
        String serviceId = (String) ctx.get(SERVICE_ID);
        if (serviceId == null) {
            log.debug("No service id found in request context {}", ctx);
        }
        return serviceId;
    }

    /**
     * Builds a cache key from request.
     */
    protected String cacheKey(HttpServletRequest req) {
        return req.getRequestURI();
    }

    /**
     * Returns the cache defined for this context or null if there is none.
     * @param ctx
     */
    protected Cache cache(RequestContext ctx) {
        String serviceId = serviceId(ctx);
        if (serviceId != null) {
            return cacheManager.getCache(serviceId);
        }
        return null;
    }


    protected String getVerb(HttpServletRequest request) {
        String method = request.getMethod();
        if (method == null) {
            return "GET";
        }
        return method;
    }

}
