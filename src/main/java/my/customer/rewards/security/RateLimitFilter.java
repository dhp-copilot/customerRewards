package my.customer.rewards.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import my.customer.rewards.config.RateLimitProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimitProperties properties;
    private final ConcurrentMap<String, BucketEntry> buckets = new ConcurrentHashMap<String, BucketEntry>();

    public RateLimitFilter() {
        this(new RateLimitProperties());
    }

    @Autowired
    public RateLimitFilter(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!properties.isEnabled()
                || !request.getRequestURI().startsWith("/api/")
                || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        long now = System.nanoTime();
        evictExpiredBuckets(now);
        String key = clientKey(request);
        BucketEntry entry = bucketFor(key, now);
        entry.lastAccessNanos = now;
        ConsumptionProbe probe = entry.bucket.tryConsumeAndReturnRemaining(1);
        setRateHeaders(response, probe);
        if (!probe.isConsumed()) {
            long retryAfter = secondsCeiling(probe.getNanosToWaitForRefill());
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.setHeader("X-Request-Path", request.getRequestURI());
            ErrorResponseWriter.write(response, 429, "Rate limit exceeded");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private BucketEntry bucketFor(String key, long now) {
        BucketEntry existing = buckets.get(key);
        if (existing != null) {
            return existing;
        }
        synchronized (buckets) {
            existing = buckets.get(key);
            if (existing != null) {
                return existing;
            }
            evictExpiredBuckets(now);
            while (buckets.size() >= properties.getMaxLocalBuckets()) {
                evictOldestBucket();
            }
            BucketEntry created = new BucketEntry(newBucket(), now);
            buckets.put(key, created);
            return created;
        }
    }

    private Bucket newBucket() {
        Duration duration = properties.getRefillDuration();
        Bandwidth limit = Bandwidth.classic(properties.getCapacity(),
                Refill.intervally(properties.getRefillTokens(), duration));
        return Bucket4j.builder().addLimit(limit).build();
    }

    private void setRateHeaders(HttpServletResponse response, ConsumptionProbe probe) {
        long remaining = Math.max(0, probe.getRemainingTokens());
        long reset = secondsCeiling(probe.getNanosToWaitForRefill());
        response.setHeader("RateLimit-Limit", String.valueOf(properties.getCapacity()));
        response.setHeader("RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("RateLimit-Reset", String.valueOf(reset));
        response.setHeader("X-RateLimit-Limit", String.valueOf(properties.getCapacity()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(reset));
    }

    private long secondsCeiling(long nanos) {
        if (nanos <= 0) {
            return 0;
        }
        return (nanos + 999999999L) / 1000000000L;
    }

    private String clientKey(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr();
        if (properties.isTrustForwardedFor() && isTrustedProxy(remoteAddress)) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.trim().isEmpty()) {
                String first = forwarded.split(",")[0].trim();
                if (!first.isEmpty()) {
                    return first;
                }
            }
        }
        return remoteAddress == null || remoteAddress.trim().isEmpty() ? "unknown" : remoteAddress;
    }

    private boolean isTrustedProxy(String remoteAddress) {
        if (remoteAddress == null) {
            return false;
        }
        for (String configured : properties.getTrustedProxies()) {
            if (matchesAddress(remoteAddress, configured)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesAddress(String address, String configured) {
        if (configured == null || configured.trim().isEmpty()) {
            return false;
        }
        String value = configured.trim();
        try {
            if (value.indexOf('/') < 0) {
                return InetAddress.getByName(value).equals(InetAddress.getByName(address));
            }
            String[] parts = value.split("/", 2);
            InetAddress network = InetAddress.getByName(parts[0]);
            InetAddress candidate = InetAddress.getByName(address);
            int prefix = Integer.parseInt(parts[1]);
            byte[] networkBytes = network.getAddress();
            byte[] candidateBytes = candidate.getAddress();
            if (networkBytes.length != candidateBytes.length
                    || prefix < 0 || prefix > networkBytes.length * 8) {
                return false;
            }
            int fullBytes = prefix / 8;
            int remainingBits = prefix % 8;
            for (int i = 0; i < fullBytes; i++) {
                if (networkBytes[i] != candidateBytes[i]) {
                    return false;
                }
            }
            if (remainingBits == 0) {
                return true;
            }
            int mask = 0xFF << (8 - remainingBits);
            return (networkBytes[fullBytes] & mask) == (candidateBytes[fullBytes] & mask);
        } catch (UnknownHostException | NumberFormatException ex) {
            return false;
        }
    }

    private void evictOldestBucket() {
        String oldestKey = null;
        long oldest = Long.MAX_VALUE;
        for (Map.Entry<String, BucketEntry> candidate : buckets.entrySet()) {
            if (candidate.getValue().lastAccessNanos < oldest) {
                oldest = candidate.getValue().lastAccessNanos;
                oldestKey = candidate.getKey();
            }
        }
        if (oldestKey != null) {
            buckets.remove(oldestKey);
        }
    }

    void evictExpiredBuckets(long now) {
        long expiration = expirationNanos();
        for (Map.Entry<String, BucketEntry> entry : buckets.entrySet()) {
            if (now - entry.getValue().lastAccessNanos >= expiration) {
                buckets.remove(entry.getKey(), entry.getValue());
            }
        }
    }

    private long expirationNanos() {
        Duration expiration = properties.getBucketExpiration();
        if (expiration == null || expiration.isZero() || expiration.isNegative()) {
            return properties.getRefillDuration().toNanos();
        }
        return expiration.toNanos();
    }

    int bucketCount() {
        return buckets.size();
    }

    private static final class BucketEntry {
        private final Bucket bucket;
        private volatile long lastAccessNanos;

        private BucketEntry(Bucket bucket, long lastAccessNanos) {
            this.bucket = bucket;
            this.lastAccessNanos = lastAccessNanos;
        }
    }
}
