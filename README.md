# SAPRFCProxyServer
A Server to use as proxy for SAP RFC calls from the Talend components tSAPRFCConnection
The service accepts the credentials from the calling SAP RFC component.

It is highly recommended to protect this service with a forward-proxy like ngix and establish SSL and IP range filtering with a firwall.
The request contains sensible information which must be protected.

Start the service with:

```java -jar saprfcproxyserver-1.1.jar```

To stop the serveice simply kill the process.

Help page output with parameter -h or --help

```
usage: java -jar saprfcproxy-<version>.jar
 -h,--help                  Print help to console, do nothing else.
 -p,--port <arg>            Port of the server
 -v,--verbose               Print statements to console
```

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
  "limit":1000
}
```

The response is 200 and as payload a json array of array of string values according to the field list given in the request.
If the request failed: status: 400 with a error message.
Response Content-Type is: application/json

## Test of the presence of the service

GET ```/ping```

Response: 200 with body Content-Type: text/plain

```pong```
