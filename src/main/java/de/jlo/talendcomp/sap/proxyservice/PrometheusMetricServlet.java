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
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.jetty.ee10.servlet.DefaultServlet;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

public class PrometheusMetricServlet extends DefaultServlet {

	private static final long serialVersionUID = 1L;
	private CollectorRegistry registry;

	/**
	 * Construct a MetricsServlet for the default registry.
	 */
	public PrometheusMetricServlet() {
		this(CollectorRegistry.defaultRegistry);
	}

	/**
	 * Construct a MetricsServlet for the given registry.
	 */
	public PrometheusMetricServlet(CollectorRegistry registry) {
		this.registry = registry;
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(TextFormat.CONTENT_TYPE_004);

		Writer writer = resp.getWriter();
		try {
			TextFormat.write004(writer, registry.filteredMetricFamilySamples(parse(req)));
			writer.flush();
		} finally {
			writer.close();
		}
	}

	private Set<String> parse(HttpServletRequest req) {
		String[] includedParam = req.getParameterValues("name[]");
		if (includedParam == null) {
			return Collections.emptySet();
		} else {
			return new HashSet<String>(Arrays.asList(includedParam));
		}
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)	throws ServletException, IOException {
		doGet(req, resp);
	}

}
