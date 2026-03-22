# Startup Order

1. Start MySQL service.
2. Execute db/schema.sql once.
3. Start RMI server.
4. Start Chat server (includes status socket on 9101).
5. Deploy and start Tomcat.

## Ports
- RMI: 1099
- Chat TCP: 9100
- Chat status: 9101
- Multicast: 230.0.0.1:8888
