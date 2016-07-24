package com.mycompany.myapp.gateway.caching;

import com.mycompany.myapp.config.JHipsterProperties;
import com.netflix.zuul.context.RequestContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static com.mycompany.myapp.gateway.caching.CachingBaseFilter.CACHE_HIT;
import static com.mycompany.myapp.gateway.caching.CachingBaseFilter.SERVICE_ID;
import static org.assertj.core.util.Lists.newArrayList;


public class CachingPreFilterTest {
    private CachingPreFilter filter;

    private RequestContext context;
    private ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
    ;

    @Before
    public void init() {
        JHipsterProperties jHipsterProperties = new JHipsterProperties();
        filter = new CachingPreFilter(cacheManager, jHipsterProperties);

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
    public void testShouldFilterTrueWhenGetAndCache() throws Exception {
        context.setRequest(new MockHttpServletRequest("GET", "/srv1/api/test"));
        String serviceId = "srv1";
        context.set(SERVICE_ID, serviceId);

        cacheManager.setCacheNames(newArrayList(serviceId));

        Assert.assertTrue(filter.shouldFilter());
    }

    @Test
    public void testRunEmptyCache() throws Exception {
        String uri = "/srv1/api/test";
        context.setRequest(new MockHttpServletRequest("GET", uri));
        String serviceId = "srv1";
        context.set(SERVICE_ID, serviceId);

        cacheManager.setCacheNames(newArrayList(serviceId));

        filter.run();

        Assert.assertFalse("Context should have registered no cache hit", context.getBoolean(CACHE_HIT));
    }

    @Test
    public void testRunCacheHit() throws Exception {
        String uri = "/srv1/api/test";
        context.setRequest(new MockHttpServletRequest("GET", uri));
        String serviceId = "srv1";
        context.set(SERVICE_ID, serviceId);

        cacheManager.setCacheNames(newArrayList(serviceId));
        cacheManager.getCache(serviceId).put(uri, new MockHttpServletResponse());

        filter.run();

        Assert.assertTrue("Tell post filter", context.getBoolean(CACHE_HIT));
    }


}
