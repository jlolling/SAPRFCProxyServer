# SAPRFCProxyServer

It is highly recommended to protect this service with a forward-proxy like ngix and establish SSL and IP range filtering with a firwall.
The request contains sensible information which must be protected.

## Dependencies
This service needs the sapjco3.jar and a native lib according to the system OS. 

You can put both files into the folder of the saprfcproxyserver-{version}.jar.

Please download both from the SAP support: [SAP JCo download](https://support.sap.com/en/product/connectors/jco.html)

This webservice is used currently by the Talend components tSAPRFCConnection and tSAPRFCTableInput.

https://github.com/jlolling/talendcomp_tSAPRFC

# Start the service

```java -jar saprfcproxyserver-1.1.jar```

To stop the service simply kill the process.

Help page output with parameter -h or --help

```
usage: java -jar saprfcproxy-<version>.jar
 -h,--help                  Print help to console, do nothing else.
 -p,--port <arg>            Port of the server
 -v,--verbose               Print statements to console
```

The service use per default port 9999


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
