# SAPRFCProxyServer
A Server to use as proxy for SAP RFC calls from the Talend components tSAPRFCConnection
The service accepts the credentials from the calling SAP RFC component.

Start the service with:

```java -jar saprfcproxyserver-1.0.jar```

To stop the serveice simply kill the process.

Help page output with parameter -h or --help

```
usage: java -jar saprfcproxy-<version>.jar
 -h,--help                  Print help to console, do nothing else.
 -p,--port <arg>            Port of the server
 -v,--verbose               Print statements to console
```
