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

import java.lang.management.ManagementFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import io.prometheus.client.hotspot.DefaultExports;

public class Main {
	
	public static Server server = null;
	private static int port = 9999;
	private static boolean verbose = false;
	private static String propertiesFileDir = null;
	private static String buckets = null;

	public static void start() throws Exception {
		if (port < 1) {
			throw new IllegalArgumentException("Port must be greater 0");
		}
		server = new Server(port);
		MBeanContainer mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
		server.addEventListener(mbContainer);
		server.addBean(mbContainer);
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		server.setHandler(context);
		PrometheusMetricsFilter pm = new PrometheusMetricsFilter();
		pm.setTimebucketsStr(buckets);
		context.addFilter(new FilterHolder(pm), "/*", null);
		if (verbose) {
			System.out.println("Add filter: PrometheusMetricsFilter at pattern: /*");
		}
		// Add SAP RFC servlet
		SAPRFCTableInputServlet tableInputServlet = new SAPRFCTableInputServlet();
		tableInputServlet.setPropertyFileDir(propertiesFileDir);
		tableInputServlet.setup();
		tableInputServlet.setLogStatements(verbose);
		if (verbose) {
			System.out.println("Add servlet: SAPRFCTableInputServlet at path: /tableinput");
		}
		context.addServlet(new ServletHolder(tableInputServlet), "/tableinput");
		SAPRFCPingServlet pingServlet = new SAPRFCPingServlet();
		tableInputServlet.setPropertyFileDir(propertiesFileDir);
		pingServlet.setup();
		pingServlet.setLogStatements(verbose);
		context.addServlet(new ServletHolder(pingServlet), "/sap-ping");
		if (verbose) {
			System.out.println("Add servlet: SAPRFCPingServlet at path: /sap-ping");
		}
		context.addServlet(new ServletHolder(new ShutdownServlet()), "/shutdown");
		if (verbose) {
			System.out.println("Add servlet: ShutdownServlet at path: /shutdown");
		}
		context.addServlet(new ServletHolder(new PingServlet()), "/ping");
		if (verbose) {
			System.out.println("Add servlet: PingServlet at path: /ping");
		}
		context.addServlet(new ServletHolder(new PrometheusMetricServlet()), "/metrics");
		DefaultExports.initialize();
		if (verbose) {
			System.out.println("Add servlet: PrometheusMetricServlet at path: /metrics");
		}
		server.setStopAtShutdown(true);
		// Start the webserver.
		server.start();
	}
	
	public static void stop() {
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	public static void main(String[] args) throws Exception {
    	Options options = new Options();
    	options.addOption("p", "port", true, "Port of the server");
    	options.addOption("v", "verbose", false, "Print statements to console");
    	options.addOption("h", "help", false, "Print help to console, do nothing else.");
    	options.addOption("b", "buckets", true, "Buckets for measure and count the request durations");
//    	options.addOption("d", "dest-prop-dir", true, "Dir for destination properties files");
    	CommandLineParser parser = new DefaultParser();
    	CommandLine cmd = parser.parse( options, args);
    	String portStr = cmd.getOptionValue('p', "9999");
    	verbose = cmd.hasOption('v');
    	boolean help = cmd.hasOption('h');
    	if (help) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar saprfcproxy-<version>.jar", options);
			System.exit(0);
    	}
    	try {
    		port = Integer.valueOf(portStr);
    	} catch (Exception e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar saprfcproxy-<version>.jar", options);
			System.exit(4);
    	}
    	propertiesFileDir = cmd.getOptionValue('d');
    	buckets = cmd.getOptionValue('b');
    	System.out.println("Start SAP RFC Proxy Server at port: " + port);
    	start();
	}

}