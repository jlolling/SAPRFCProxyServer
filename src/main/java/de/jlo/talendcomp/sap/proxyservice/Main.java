package de.jlo.talendcomp.sap.proxyservice;

import java.lang.management.ManagementFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Main {
	
	public static Server server = null;
	private static int port = 9999;
	private static boolean verbose = false;
	private static String propertiesFileDir = null;

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
		context.addServlet(new ServletHolder(pingServlet), "/ping");
		if (verbose) {
			System.out.println("Add servlet: SAPRFCPingServlet at path: /ping");
		}
		context.addServlet(new ServletHolder(new ShutdownServlet()), "/shutdown");
		if (verbose) {
			System.out.println("Add servlet: ShutdownServlet at path: /shutdown");
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
    	options.addOption("d", "dest-prop-dir", true, "Dir for destination properties files");
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
    	System.out.println("Start SAP RFC Proxy Server at port: " + port);
    	start();
	}

}