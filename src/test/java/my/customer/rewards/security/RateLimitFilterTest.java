package my.customer.rewards.security;

import my.customer.rewards.config.RateLimitProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import java.time.Duration;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitFilterTest {
    @Test
    void returnsStructured429AfterOneHundredRequestsPerClient() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();
        FilterChain chain = (request, response) -> { };
        MockHttpServletRequest request = request("10.10.10.10", null);
        for (int i = 0; i < 100; i++) {
            filter.doFilter(request, new MockHttpServletResponse(), chain);
        }
        MockHttpServletResponse limited = new MockHttpServletResponse();
        filter.doFilter(request, limited, chain);
        assertEquals(429, limited.getStatus());
        assertTrue(limited.getContentAsString().contains("\"status\":429"));
        assertNotNull(limited.getHeader("Retry-After"));
        assertEquals("100", limited.getHeader("RateLimit-Limit"));
        assertEquals("0", limited.getHeader("RateLimit-Remaining"));
    }

    @Test
    void doesNotTrustForwardedForFromAnUntrustedPeer() throws Exception {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setCapacity(1);
        properties.setRefillTokens(1);
        properties.setRefillDuration(Duration.ofHours(1));
        properties.setTrustForwardedFor(true);
        properties.setTrustedProxies(Collections.singletonList("192.0.2.10"));
        RateLimitFilter filter = new RateLimitFilter(properties);
        FilterChain chain = (request, response) -> { };

        filter.doFilter(request("192.0.2.20", "198.51.100.1"),
                new MockHttpServletResponse(), chain);
        MockHttpServletResponse limited = new MockHttpServletResponse();
        filter.doFilter(request("192.0.2.20", "198.51.100.2"), limited, chain);

        assertEquals(429, limited.getStatus());
    }

    @Test
    void trustedProxyCanSeparateForwardedClients() throws Exception {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setCapacity(1);
        properties.setRefillTokens(1);
        properties.setRefillDuration(Duration.ofHours(1));
        properties.setTrustForwardedFor(true);
        properties.setTrustedProxies(Collections.singletonList("192.0.2.10"));
        RateLimitFilter filter = new RateLimitFilter(properties);
        FilterChain chain = (request, response) -> { };

        filter.doFilter(request("192.0.2.10", "198.51.100.1"),
                new MockHttpServletResponse(), chain);
        MockHttpServletResponse second = new MockHttpServletResponse();
        filter.doFilter(request("192.0.2.10", "198.51.100.2"), second, chain);

        assertEquals(200, second.getStatus());
        assertEquals("1", second.getHeader("RateLimit-Limit"));
    }

    @Test
    void localBucketsAreBoundedAndExpired() throws Exception {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setCapacity(2);
        properties.setRefillTokens(2);
        properties.setRefillDuration(Duration.ofHours(1));
        properties.setMaxLocalBuckets(2);
        properties.setBucketExpiration(Duration.ofNanos(1));
        RateLimitFilter filter = new RateLimitFilter(properties);
        FilterChain chain = (request, response) -> { };

        filter.doFilter(request("192.0.2.1", null), new MockHttpServletResponse(), chain);
        filter.doFilter(request("192.0.2.2", null), new MockHttpServletResponse(), chain);
        filter.doFilter(request("192.0.2.3", null), new MockHttpServletResponse(), chain);

        assertTrue(filter.bucketCount() <= 2);
        filter.evictExpiredBuckets(System.nanoTime());
        assertEquals(0, filter.bucketCount());
    }

    private MockHttpServletRequest request(String remoteAddress, String forwardedAddress) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/rewards/alice/total");
        request.setRemoteAddr(remoteAddress);
        if (forwardedAddress != null) {
            request.addHeader("X-Forwarded-For", forwardedAddress);
        }
        return request;
    }
}
