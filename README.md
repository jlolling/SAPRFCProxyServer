# SAPRFCProxyServer

This is a RESTful web service which runs SAP RFC calls using the RFC function RFC_READ_TABLE. The service takes the query as json POST request.
The advatage of using this service is:
* Allows to have only one place where the SAP JCo driver is needed
* Simplifies the ETL jobs because they do not need the SAP JCo driver
* Run the SAP RFC queries from one server which is enabled to do that (mostly SAP services are protected by IP filtering firewalls)
* The service has maintenance features allows runnig it behind load balances

It is highly recommended to protect this service with a forward-proxy like ngix and establish SSL and IP range filtering with a firwall.
The request contains sensible information which must be protected.

## Dependencies
This service needs the sapjco3.jar and a native lib according to the system OS.
You can put both files into the folder of the saprfcproxyserver-{version}.jar.

Please download both from the SAP support: [SAP JCo download](https://support.sap.com/en/product/connectors/jco.html)

This webservice is used currently by the Talend components tSAPRFCConnection and tSAPRFCTableInput: [Github repo](https://github.com/jlolling/talendcomp_tSAPRFC)

Because this service use the function RFC_READ_TABLE which is installed in all SAP systems, there are no dependencies releated to the SAP system. This is important in use cases in which you cannot install any more convienent functions in the SAP system (the author of this service was faced exactly with this problem).

## Logging

You can configure the usage of Log4J2 for this service with the environment variable log4j2.configurationFile pointing to a log4j2 config file (with the formats: xml, yml, properties, json). If the file is located within the root dir of the service, simple setup the environment with the name of the file, otherwise set the absolute path of the configuration file. In the source dir of the project you will find a good start of a log4j2.xml file.
The example log file creates a log file service.log in the sub dir log/ and roll every day the log file and archive them in a folder log/yyyy-MM/service-yyyy-MM-dd_<index>.log

# Start the service

```java -Dlog4j2.configurationFile=log4j2.xml -jar saprfcproxyserver-2.2.jar```

To stop the service simply kill the process.

Help page output with parameter -h or --help

```
usage: java -jar saprfcproxy-<version>.jar
 -h,--help                  Print help to console, do nothing else.
 -p,--port <arg>            Port of the server
 -v,--verbose               Print statements to console
```

The service use per default port 9999

# Stop the service

With the help of a REST client (like Talend API Tester or SoapUI) you can call the endpoint and this will shutdown the service.
```OPTIONS http://<yourserver>:<yourport>/shutdown```

# Endpoints
All endpoints expects application/json as Content-Type

## Check the SAP connection

POST ```/sap-ping```

Check the connection to the specified SAP server
The payload must provide the destination.
The password can be send clear text or ecrypted in the Talend standard way.
There are 2 different ways to connect to a SAP server.

* Application Server:

```
{
  "destination":{
  "destinationType":"application_server",
  "host":"192.168.2.34",
  "client":"003",
  "systemNumber":"03",
  "language":"DE",
  "user":"your-user",
  "password":"your-password"
  }
}
```

* Message Server

```
{
  "destination":{
  "destinationType":"message_server",
  "host":"192.168.2.34",
  "client":"003",
  "r3name":"PR",
  "group":"Public",
  "language":"EN",
  "user":"your-user",
  "password":"your-password"
  }
}
```

Response is 202 without payload if test passed and 400 with the message from SAP if test failed.

## Run the query

POST ```/tableinput```

Run the actual query.
The payload must provide the destination and the attributes to describe the actual query.
Here an example speaking to an application server. Please refer to the sap-ping request for the message-server payload.
Only the attributes: tableName and fields is mandatory. Attributes filter, offset, limit can be ommitted or set to null.

```
{
  "destination":{
  "destinationType":"application_server",
  "host":"192.168.2.34",
  "client":"003",
  "systemNumber":"03",
  "language":"DE",
  "user":"your-user",
  "password":"your-password"
  },
  "tableName":"KNB1",
  "fields":["KUNNR","BUKRS"],
  "filter":"KUNNR > 1000",
  "offset":100,
  "limit":5
}
```
Because of the limitation of the used so called standard component RFC_READ_TABLE the filter expression cannot be longer as 72 chars.
If longer a new OPTION line must be set. The service allows to split long filter expression with the delimiter ; into parts smaller than 72 chars.
You cannot split within a comparison and not within a string literal but you can split between comparisions and within literal lists (in operator).

Here an example:

```"KUNNR in (100,200,300,400,500) ; or KUNNR in (600,7000,800,900); and BURKS = 'BLN'"```

Please be aware SAP expects between identifiers and operators always a space!

* Response OK:

Status: 200, 
Content-Type: application/json

Body example:
```
[
["0000006392","0010051021"],
["0000022047","0010050266"],
["0000023825","0010012228"],
["0000030460","0010183904"],
["0000032123","0010012228"]
]
```
* Request failed:
Status: 400, 
Content-Type: text/html,
Body: html coded error message


## Test of the presence of the service

GET ```/ping```

Response: 200 with body Content-Type: text/plain

```pong```

## Get metrics in Prometheus format

GET ```/metrics```

Response: 200 with body Content-Type: text/plain

JVM and performance metrics about the service endpoints

Example:

```
# HELP jvm_memory_bytes_used Used bytes of a given JVM memory area.
# TYPE jvm_memory_bytes_used gauge
jvm_memory_bytes_used{area="heap",} 1.060970648E9
jvm_memory_bytes_used{area="nonheap",} 5.1382E7
# HELP jvm_memory_bytes_committed Committed (bytes) of a given JVM memory area.
# TYPE jvm_memory_bytes_committed gauge
jvm_memory_bytes_committed{area="heap",} 1.291845632E9
jvm_memory_bytes_committed{area="nonheap",} 5.484544E7
# HELP jvm_memory_bytes_max Max (bytes) of a given JVM memory area.
# TYPE jvm_memory_bytes_max gauge
jvm_memory_bytes_max{area="heap",} 1.7179869184E10
jvm_memory_bytes_max{area="nonheap",} -1.0
# HELP jvm_memory_bytes_init Initial bytes of a given JVM memory area.
# TYPE jvm_memory_bytes_init gauge
jvm_memory_bytes_init{area="heap",} 1.073741824E9
jvm_memory_bytes_init{area="nonheap",} 7667712.0
# HELP jvm_memory_pool_bytes_used Used bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_bytes_used gauge
jvm_memory_pool_bytes_used{pool="CodeHeap 'non-nmethods'",} 1249152.0
jvm_memory_pool_bytes_used{pool="Metaspace",} 2.99406E7
jvm_memory_pool_bytes_used{pool="CodeHeap 'profiled nmethods'",} 1.2355968E7
jvm_memory_pool_bytes_used{pool="Compressed Class Space",} 2830072.0
jvm_memory_pool_bytes_used{pool="G1 Eden Space",} 5.62036736E8
jvm_memory_pool_bytes_used{pool="G1 Old Gen",} 4.94739608E8
jvm_memory_pool_bytes_used{pool="G1 Survivor Space",} 4194304.0
jvm_memory_pool_bytes_used{pool="CodeHeap 'non-profiled nmethods'",} 5006208.0
# HELP jvm_memory_pool_bytes_committed Committed bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_bytes_committed gauge
jvm_memory_pool_bytes_committed{pool="CodeHeap 'non-nmethods'",} 2555904.0
jvm_memory_pool_bytes_committed{pool="Metaspace",} 3.1453184E7
jvm_memory_pool_bytes_committed{pool="CodeHeap 'profiled nmethods'",} 1.2386304E7
jvm_memory_pool_bytes_committed{pool="Compressed Class Space",} 3403776.0
jvm_memory_pool_bytes_committed{pool="G1 Eden Space",} 6.03979776E8
jvm_memory_pool_bytes_committed{pool="G1 Old Gen",} 6.83671552E8
jvm_memory_pool_bytes_committed{pool="G1 Survivor Space",} 4194304.0
jvm_memory_pool_bytes_committed{pool="CodeHeap 'non-profiled nmethods'",} 5046272.0
# HELP jvm_memory_pool_bytes_max Max bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_bytes_max gauge
jvm_memory_pool_bytes_max{pool="CodeHeap 'non-nmethods'",} 5898240.0
jvm_memory_pool_bytes_max{pool="Metaspace",} -1.0
jvm_memory_pool_bytes_max{pool="CodeHeap 'profiled nmethods'",} 1.2288E8
jvm_memory_pool_bytes_max{pool="Compressed Class Space",} 1.073741824E9
jvm_memory_pool_bytes_max{pool="G1 Eden Space",} -1.0
jvm_memory_pool_bytes_max{pool="G1 Old Gen",} 1.7179869184E10
jvm_memory_pool_bytes_max{pool="G1 Survivor Space",} -1.0
jvm_memory_pool_bytes_max{pool="CodeHeap 'non-profiled nmethods'",} 1.2288E8
# HELP jvm_memory_pool_bytes_init Initial bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_bytes_init gauge
jvm_memory_pool_bytes_init{pool="CodeHeap 'non-nmethods'",} 2555904.0
jvm_memory_pool_bytes_init{pool="Metaspace",} 0.0
jvm_memory_pool_bytes_init{pool="CodeHeap 'profiled nmethods'",} 2555904.0
jvm_memory_pool_bytes_init{pool="Compressed Class Space",} 0.0
jvm_memory_pool_bytes_init{pool="G1 Eden Space",} 5.4525952E7
jvm_memory_pool_bytes_init{pool="G1 Old Gen",} 1.019215872E9
jvm_memory_pool_bytes_init{pool="G1 Survivor Space",} 0.0
jvm_memory_pool_bytes_init{pool="CodeHeap 'non-profiled nmethods'",} 2555904.0
# HELP jvm_info JVM version info
# TYPE jvm_info gauge
jvm_info{version="11.0.11+9-adhoc..jdk11u",vendor="OpenLogic",runtime="OpenJDK Runtime Environment",} 1.0
# HELP process_cpu_seconds_total Total user and system CPU time spent in seconds.
# TYPE process_cpu_seconds_total counter
process_cpu_seconds_total 558.859375
# HELP process_start_time_seconds Start time of the process since unix epoch in seconds.
# TYPE process_start_time_seconds gauge
process_start_time_seconds 1.680772910313E9
# HELP jvm_gc_collection_seconds Time spent in a given JVM garbage collector in seconds.
# TYPE jvm_gc_collection_seconds summary
jvm_gc_collection_seconds_count{gc="G1 Young Generation",} 82.0
jvm_gc_collection_seconds_sum{gc="G1 Young Generation",} 6.136
jvm_gc_collection_seconds_count{gc="G1 Old Generation",} 0.0
jvm_gc_collection_seconds_sum{gc="G1 Old Generation",} 0.0
# HELP jvm_classes_loaded The number of classes that are currently loaded in the JVM
# TYPE jvm_classes_loaded gauge
jvm_classes_loaded 3799.0
# HELP jvm_classes_loaded_total The total number of classes that have been loaded since the JVM has started execution
# TYPE jvm_classes_loaded_total counter
jvm_classes_loaded_total 4089.0
# HELP jvm_classes_unloaded_total The total number of classes that have been unloaded since the JVM has started execution
# TYPE jvm_classes_unloaded_total counter
jvm_classes_unloaded_total 290.0
# HELP jvm_buffer_pool_used_bytes Used bytes of a given JVM buffer pool.
# TYPE jvm_buffer_pool_used_bytes gauge
jvm_buffer_pool_used_bytes{pool="mapped",} 0.0
jvm_buffer_pool_used_bytes{pool="direct",} 122928.0
# HELP jvm_buffer_pool_capacity_bytes Bytes capacity of a given JVM buffer pool.
# TYPE jvm_buffer_pool_capacity_bytes gauge
jvm_buffer_pool_capacity_bytes{pool="mapped",} 0.0
jvm_buffer_pool_capacity_bytes{pool="direct",} 122928.0
# HELP jvm_buffer_pool_used_buffers Used buffers of a given JVM buffer pool.
# TYPE jvm_buffer_pool_used_buffers gauge
jvm_buffer_pool_used_buffers{pool="mapped",} 0.0
jvm_buffer_pool_used_buffers{pool="direct",} 9.0
# HELP jvm_threads_current Current thread count of a JVM
# TYPE jvm_threads_current gauge
jvm_threads_current 16.0
# HELP jvm_threads_daemon Daemon thread count of a JVM
# TYPE jvm_threads_daemon gauge
jvm_threads_daemon 6.0
# HELP jvm_threads_peak Peak thread count of a JVM
# TYPE jvm_threads_peak gauge
jvm_threads_peak 20.0
# HELP jvm_threads_started_total Started thread count of a JVM
# TYPE jvm_threads_started_total counter
jvm_threads_started_total 48.0
# HELP jvm_threads_deadlocked Cycles of JVM-threads that are in deadlock waiting to acquire object monitors or ownable synchronizers
# TYPE jvm_threads_deadlocked gauge
jvm_threads_deadlocked 0.0
# HELP jvm_threads_deadlocked_monitor Cycles of JVM-threads that are in deadlock waiting to acquire object monitors
# TYPE jvm_threads_deadlocked_monitor gauge
jvm_threads_deadlocked_monitor 0.0
# HELP request_status_total HTTP status codes of requests
# TYPE request_status_total counter
request_status_total{path="/sap-ping",method="POST",status="202",} 58.0
request_status_total{path="/tableinput",method="POST",status="200",} 5947.0
# HELP duration Request duration
# TYPE duration histogram
duration_bucket{path="/sap-ping",method="POST",le="0.001",} 0.0
duration_bucket{path="/sap-ping",method="POST",le="0.1",} 36.0
duration_bucket{path="/sap-ping",method="POST",le="1.0",} 54.0
duration_bucket{path="/sap-ping",method="POST",le="10.0",} 58.0
duration_bucket{path="/sap-ping",method="POST",le="100.0",} 58.0
duration_bucket{path="/sap-ping",method="POST",le="+Inf",} 58.0
duration_count{path="/sap-ping",method="POST",} 58.0
duration_sum{path="/sap-ping",method="POST",} 12.983635545000006
duration_bucket{path="/tableinput",method="POST",le="0.001",} 0.0
duration_bucket{path="/tableinput",method="POST",le="0.1",} 4913.0
duration_bucket{path="/tableinput",method="POST",le="1.0",} 5674.0
duration_bucket{path="/tableinput",method="POST",le="10.0",} 5886.0
duration_bucket{path="/tableinput",method="POST",le="100.0",} 5942.0
duration_bucket{path="/tableinput",method="POST",le="+Inf",} 5947.0
duration_count{path="/tableinput",method="POST",} 5947.0
duration_sum{path="/tableinput",method="POST",} 3415.6584721810027
```
