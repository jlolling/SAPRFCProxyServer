/**
 * Copyright 2023 Jan Lolling jan.lolling@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * A simplified filter to collect the count and duration of the requests
 * @author jan.lolling@gmail.com
 */
public class PrometheusMetricsFilter implements Filter {

    private Histogram histogram = null;
    private Counter statusCounter = null;
    private int pathComponents = 1;
    boolean stripContextPath = false;
    private String timebucketsStr = "0.001,0.1,1,10,100,1000";

	@Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Histogram.Builder builder = Histogram.build()
                .labelNames("path", "method");
        String[] bucketParams = timebucketsStr.split(",");
        double[] buckets = new double[bucketParams.length];

        for (int i = 0; i < bucketParams.length; i++) {
            buckets[i] = Double.parseDouble(bucketParams[i]);
        }
        if (buckets != null && buckets.length > 0) {
            builder = builder.buckets(buckets);
        }
        histogram = builder
                .help("Request duration")
                .name("duration")
                .register();
        statusCounter = Counter.build("request_status_total", "HTTP status codes of requests")
                .labelNames("path", "method", "status")
                .register();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest) || !(servletResponse instanceof HttpServletResponse)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        String requestUri = httpRequest.getRequestURI();
        boolean measureIt = false; 
        if (requestUri != null) {
        	measureIt = requestUri.contains("/tableinput") || requestUri.contains("/sap-ping");
        }
        MetricData data = null;
    	if (measureIt) {
    		data = startTimer(httpRequest);
    	}
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
        	if (data != null) {
            	observeDuration(data, (HttpServletResponse) servletResponse);
        	}
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

    public String getTimebucketsStr() {
		return timebucketsStr;
	}

	public void setTimebucketsStr(String timebucketsStr) {
		if (timebucketsStr != null && timebucketsStr.length() > 0) {
			this.timebucketsStr = timebucketsStr;
		}
	}

}
