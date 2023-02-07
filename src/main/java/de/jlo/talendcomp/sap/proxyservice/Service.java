package de.jlo.talendcomp.sap.proxyservice;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Service {
	
	private Server server = null;

	public void start(int port, boolean log) throws Exception {
		if (port < 1) {
			throw new IllegalArgumentException("Port must be greater 0");
		}
		server = new Server(port);
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		server.setHandler(context);
		// Add SAP RFC servlet
		SAPRFCTableInputServlet tableInputServlet = new SAPRFCTableInputServlet();
		tableInputServlet.setup();
		tableInputServlet.setLogStatements(log);
		if (log) {
			System.out.println("Add servlet: SAPRFCTableInputServlet at path: /tableinput");
		}
		context.addServlet(new ServletHolder(tableInputServlet), "/tableinput");
		server.setStopAtShutdown(true);
		// Start the webserver.
		server.start();
	}
	
	public void stop() {
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
    	CommandLineParser parser = new DefaultParser();
    	CommandLine cmd = parser.parse( options, args);
    	String portStr = cmd.getOptionValue('p', "9999");
    	boolean verbose = cmd.hasOption('v');
    	boolean help = cmd.hasOption('h');
    	if (help) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar saprfcproxy-<version>.jar", options);
			System.exit(0);
    	}
    	int port = 0;
    	try {
    		port = Integer.valueOf(portStr);
    	} catch (Exception e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar saprfcproxy-<version>.jar", options);
			System.exit(4);
    	}
    	Service service = new Service();
    	System.out.println("Start SAP RFC Proxy Server at port: " + port);
    	service.start(port, verbose);
	}

}