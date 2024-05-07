package inthemix.clientlogging;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Set;

@ConfigurationProperties("client.logging")
public class RestClientLoggingProperties {
    /** Headers that should be excluded from the request logging */
    private Set<String> excludeHeaders = Collections.emptySet();

    /** Whether to configure rest client request/response logging */
    private boolean enabled = true;

    public Set<String> getExcludeHeaders() {
        return excludeHeaders;
    }

    public void setExcludeHeaders(Set<String> excludeHeaders) {
        this.excludeHeaders = excludeHeaders;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
