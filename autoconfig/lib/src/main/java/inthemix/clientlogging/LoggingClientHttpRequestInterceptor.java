package inthemix.clientlogging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

/**
 * Allows logging outgoing requests and the corresponding responses.
 * Requires the use of a {@link org.springframework.http.client.BufferingClientHttpRequestFactory} to log
 * the body of received responses!
 * <p>
 * Logging is enabled by either setting the log level of the appropriate SLF4J Logger to {@code DEBUG},
 * or per request by setting a {@code X-Log-Request} or {@code X-Log-Response} request header to @{code true}.
 * The latter will cause logging to happen at the {@code INFO} rather than the {@code DEBUG} level.
 */
public class LoggingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    public static final String LOG_REQUEST_REQUEST_HEADER  = "X-Log-Request";
    public static final String LOG_RESPONSE_REQUEST_HEADER = "X-Log-Response";

    protected final Logger requestLogger;
    protected final Logger responseLogger;

    private final Set<String> loggingExcludeHeaders;

    public LoggingClientHttpRequestInterceptor(Collection<String> loggingExcludeHeaders) {
        this(LoggerFactory.getLogger("inthemix.client.RestTracing.sent"),
            LoggerFactory.getLogger("inthemix.client.RestTracing.received"),
            loggingExcludeHeaders);
    }

    /**
     * @param requestLogger the logger used to log sent requests
     * @param responseLogger the logger used to log received responses
     */
    public LoggingClientHttpRequestInterceptor(Logger requestLogger, Logger responseLogger, Collection<String> loggingExcludeHeaders) {
        this.requestLogger = requestLogger;
        this.responseLogger = responseLogger;
        this.loggingExcludeHeaders = loggingExcludeHeaders.stream().map(String::toLowerCase).collect(toSet());
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(request, response);
        return response;
    }

    protected void logRequest(HttpRequest request, byte[] body) {
        if (requestLogger.isDebugEnabled() && !noLoggingRequested(request, LOG_REQUEST_REQUEST_HEADER)) {
            requestLogger.debug(constructRequestLogMsg(request, body));
        } else if (requestLogger.isInfoEnabled() && loggingRequested(request, LOG_REQUEST_REQUEST_HEADER)) {
            requestLogger.info(constructRequestLogMsg(request, body));
        }
    }

    protected String constructRequestLogMsg(HttpRequest request, byte[] body) {
        StringBuilder builder = new StringBuilder("Sending method=").append(request.getMethod())
                .append(" request to url=").append(request.getURI());

        String headerString = request.getHeaders().entrySet().stream()
            .filter(entry -> !loggingExcludeHeaders.contains(entry.getKey().toLowerCase()))
            .map(entry -> String.format("'%s'='%s'", entry.getKey(), String.join(",", entry.getValue())))
            .collect(Collectors.joining(", "));

        if (!headerString.isEmpty()) {
            builder.append(": headers:[").append(headerString).append("]");
        }

        if (body.length > 0 && hasTextBody(request.getHeaders())) {
            String bodyText = new String(body, determineCharset(request.getHeaders()));
            builder.append(": body:[").append(bodyText).append("]");
        }
        return builder.toString();
    }

    protected void logResponse(HttpRequest request, ClientHttpResponse response) {
        if (responseLogger.isDebugEnabled() && !noLoggingRequested(request, LOG_RESPONSE_REQUEST_HEADER)) {
            try {
                responseLogger.debug(constructResponseLogMsg(request, response));
            } catch (IOException e) {
                responseLogger.warn("Failed to log response for {} request to {}", request.getMethod(), request.getURI(), e);
            }
        } else if (responseLogger.isInfoEnabled() && loggingRequested(request, LOG_RESPONSE_REQUEST_HEADER)) {
            try {
                responseLogger.info(constructResponseLogMsg(request, response));
            } catch (IOException e) {
                responseLogger.warn("Failed to log response for {} request to {}", request.getMethod(), request.getURI(), e);
            }
        }
    }

    protected String constructResponseLogMsg(HttpRequest request, ClientHttpResponse response) throws IOException {
        StringBuilder builder = new StringBuilder("Received status=\"")
            .append(response.getStatusCode().value()).append(" ").append(response.getStatusText())
            .append("\" response for method=").append(request.getMethod())
            .append(" request to url=").append(request.getURI());
        HttpHeaders responseHeaders = response.getHeaders();
        long contentLength = responseHeaders.getContentLength();

        String headerString = response.getHeaders().entrySet().stream()
            .filter(entry -> !loggingExcludeHeaders.contains(entry.getKey()))
            .map(entry -> String.format("'%s'='%s'", entry.getKey(), String.join(",", entry.getValue())))
            .collect(Collectors.joining(", "));

        if (!headerString.isEmpty()) {
            builder.append(": headers:[").append(headerString).append("]");
        }

        if (contentLength != 0) {
            if (hasTextBody(responseHeaders) && !isMockedResponse(response)) {
                String bodyText = StreamUtils.copyToString(response.getBody(), determineCharset(responseHeaders));
                builder.append(": body:[").append(bodyText).append("]");
            } else {
                if (contentLength == -1) {
                    builder.append(" with content of unknown length");
                } else {
                    builder.append(" with content of length ").append(contentLength);
                }
                MediaType contentType = responseHeaders.getContentType();
                if (contentType != null) {
                    builder.append(" and content type ").append(contentType);
                } else {
                    builder.append(" and unknown content type");
                }
            }
        }
        return builder.toString();
    }

    protected boolean hasTextBody(HttpHeaders headers) {
        MediaType contentType = headers.getContentType();
        if (contentType != null) {
            if ("text".equals(contentType.getType())) {
                return true;
            }
            String subtype = contentType.getSubtype();
            if (subtype != null) {
                return "xml".equals(subtype) || "json".equals(subtype) ||
                    subtype.endsWith("+xml") || subtype.endsWith("+json");
            }
        }
        return false;
    }

    protected Charset determineCharset(HttpHeaders headers) {
        MediaType contentType = headers.getContentType();
        if (contentType != null) {
            try {
                Charset charSet = contentType.getCharset();
                if (charSet != null) {
                    return charSet;
                }
            } catch (UnsupportedCharsetException e) {
                // ignore
            }
        }
        return StandardCharsets.UTF_8;
    }

    protected boolean loggingRequested(HttpRequest request, String header) {
        return "true".equalsIgnoreCase(request.getHeaders().getFirst(header));
    }

    protected boolean noLoggingRequested(HttpRequest request, String header) {
        return "false".equalsIgnoreCase(request.getHeaders().getFirst(header));
    }

    private boolean isMockedResponse(ClientHttpResponse response) {
        return "MockClientHttpResponse".equals(response.getClass().getSimpleName());
    }

}
