


DynDNS server, installation:

- install PowerDNS with pdns-backend-mysql;
- install poweradmin with DB tables;
- change auth data in DynDNS_server/resources/settings.properties;
- compile project;
- start DynDNS server: java -jar DynDNS_server.jar 
- check logs;

- open poweradmin, create user/password and DNS zone. Set TTL 30 for zone. 
use this data for DynDNS client;