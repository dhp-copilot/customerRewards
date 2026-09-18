package my.customer.rewards.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Local rate-limit settings. The local store is deliberately bounded and is
 * intended for a single application instance; a shared Bucket4j backend should
 * be used when the application is scaled horizontally.
 */
@Validated
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {
    private boolean enabled = true;

    @Min(1)
    private int capacity = 100;

    @Min(1)
    private int refillTokens = 100;

    private Duration refillDuration = Duration.ofMinutes(1);

    @Min(1)
    private int maxLocalBuckets = 10000;

    private Duration bucketExpiration = Duration.ofMinutes(10);

    private boolean trustForwardedFor;
    private List<String> trustedProxies = new ArrayList<String>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getRefillTokens() { return refillTokens; }
    public void setRefillTokens(int refillTokens) { this.refillTokens = refillTokens; }
    public Duration getRefillDuration() { return refillDuration; }
    public void setRefillDuration(Duration refillDuration) { this.refillDuration = refillDuration; }
    public int getMaxLocalBuckets() { return maxLocalBuckets; }
    public void setMaxLocalBuckets(int maxLocalBuckets) { this.maxLocalBuckets = maxLocalBuckets; }
    public Duration getBucketExpiration() { return bucketExpiration; }
    public void setBucketExpiration(Duration bucketExpiration) { this.bucketExpiration = bucketExpiration; }
    public boolean isTrustForwardedFor() { return trustForwardedFor; }
    public void setTrustForwardedFor(boolean trustForwardedFor) { this.trustForwardedFor = trustForwardedFor; }
    public List<String> getTrustedProxies() { return trustedProxies; }
    public void setTrustedProxies(List<String> trustedProxies) {
        this.trustedProxies = trustedProxies == null
                ? new ArrayList<String>() : new ArrayList<String>(trustedProxies);
    }
}
