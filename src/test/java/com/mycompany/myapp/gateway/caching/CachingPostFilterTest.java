package com.mycompany.myapp.gateway.caching;

import com.mycompany.myapp.config.JHipsterProperties;
import com.netflix.zuul.context.RequestContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.mock.web.MockHttpServletRequest;

import static com.mycompany.myapp.gateway.caching.CachingBaseFilter.CACHE_HIT;
import static com.mycompany.myapp.gateway.caching.CachingBaseFilter.SERVICE_ID;
import static org.assertj.core.util.Lists.newArrayList;


public class CachingPostFilterTest {
    private CachingPostFilter filter;

    private RequestContext context;
    private ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
    ;

    @Before
    public void init() {
        JHipsterProperties jHipsterProperties = new JHipsterProperties();
        filter = new CachingPostFilter(cacheManager, jHipsterProperties);

        context = new RequestContext();
        RequestContext.testSetCurrentContext(context);

    }

    @Test
    public void testShouldFilterFalseWhenNotGet() throws Exception {
        context.setRequest(new MockHttpServletRequest());

        Assert.assertFalse(filter.shouldFilter());
    }

    @Test
    public void testShouldFilterFalseWhenGetButNoCache() throws Exception {
        context.setRequest(new MockHttpServletRequest("GET", "/srv1/api/test"));

        Assert.assertFalse(filter.shouldFilter());
    }

    @Test
    public void testShouldFilterFalseWhenCacheHit() throws Exception {
        context.setRequest(new MockHttpServletRequest("GET", "/srv1/api/test"));
        String serviceId = "srv1";
        context.set(SERVICE_ID, serviceId);
        context.set(CACHE_HIT, true);

        cacheManager.setCacheNames(newArrayList(serviceId));


        Assert.assertFalse(filter.shouldFilter());
    }

    @Test
    public void testShouldFilterTrueWhenCacheNotHit() throws Exception {
        context.setRequest(new MockHttpServletRequest("GET", "/srv1/api/test"));
        String serviceId = "srv1";
        context.set(SERVICE_ID, serviceId);
        context.set(CACHE_HIT, false);

        cacheManager.setCacheNames(newArrayList(serviceId));

        Assert.assertTrue(filter.shouldFilter());
    }

}
