package de.jlo.talendcomp.sap.proxyservice;

import java.io.IOException;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 
 */
public class PrometheusMetricsFilter implements Filter {

    private Histogram histogram = null;
    private Counter statusCounter = null;
    private int pathComponents = 1;
    boolean stripContextPath = false;
    
	public PrometheusMetricsFilter() {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Histogram.Builder builder = Histogram.build()
                .labelNames("path", "method");
        histogram = builder
                .help("Request duration")
                .name("duration")
                .register();

        statusCounter = Counter.build("duration_status_total", "HTTP status codes of Request duration")
                .labelNames("path", "method", "status")
                .register();

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest) || !(servletResponse instanceof HttpServletResponse)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        MetricData data = startTimer((HttpServletRequest) servletRequest);
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
        	observeDuration(data, (HttpServletResponse) servletResponse);
        }
    }

    @Override
    public void destroy() {
    }
    
    private String getComponents(String str) {
        if (str == null || pathComponents < 1) {
            return str;
        }
        int count = 0;
        int i =  -1;
        do {
            i = str.indexOf("/", i + 1);
            if (i < 0) {
                // Path is longer than specified pathComponents.
                return str;
            }
            count++;
        } while (count <= pathComponents);
        return str.substring(0, i);
    }

    /**
     * To be called at the beginning of {@code javax.servlet.Filter.doFilter()} or
     * {@code jakarta.servlet.Filter.doFilter()}.
     */
    public MetricData startTimer(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (stripContextPath) {
            path = path.substring(request.getContextPath().length());
        }
        String components = getComponents(path);
        String method = request.getMethod();
        Histogram.Timer timer = histogram.labels(components, method).startTimer();
        return new MetricData(components, method, timer);
    }

    /**
     * To be called at the end of {@code javax.servlet.Filter.doFilter()} or
     * {@code jakarta.servlet.Filter.doFilter()}.
     */
    public void observeDuration(MetricData data, HttpServletResponse resp) {
        String status = Integer.toString(resp.getStatus());
        data.timer.observeDuration();
        statusCounter.labels(data.components, data.method, status).inc();
    }

    public static class MetricData {

        final String components;
        final String method;
        final Histogram.Timer timer;

        private MetricData(String components, String method, Histogram.Timer timer) {
            this.components = components;
            this.method = method;
            this.timer = timer;
        }
        
    }

}
