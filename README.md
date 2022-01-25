# Description

This project provides spring cloud with etcd discovery.


# Building

Run the belowing command to build and install to local maven repository

```bash
mvn clean install
```

# Using

## add dependency

Add dependency to pom.xml

```xml
<dependency>
    <groupId>com.guosen.etcd</groupId>
    <name>spring-cloud-starter-guosen-etcd-discovery</name>
    <version>1.0.0-SNAPSHOT</version>
</dependency>    
```

## configuration

Add the below configuration into properties file

| key        | descrition    |
| --------   | -----  |
| spring.cloud.etcd.discovery.service        | service name, if not set, will use  ${spring.application.name} instead     |
| spring.cloud.etcd.discovery.group       | the group of service, service will only discover the same group services. <br/> If not set, will use "default" by default   |
| spring.cloud.etcd.discovery.enable        | If you just want to subscribe, but don't want to register your service, set it to false      |


# Brief

Server Side Service registes info into etcd, prefix will be

```bash
/spring/cloud/etcd/{group}/{servicename}/{uuid}
```

The content inside is 

```json
{
    "host": "{ip}",
    "metadata": {},
    "port": {port},
    "secure": false,
    "serviceId": "{servicename}",
    "uri": "http://{ip}:{port}"
}
```

