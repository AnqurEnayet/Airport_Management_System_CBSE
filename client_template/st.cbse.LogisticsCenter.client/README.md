# new commands
Maven:
mvn clean package

java --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED --add-opens java.base/java.security=ALL-UNNAMED --add-opens java.base/sun.security.action=ALL-UNNAMED --add-opens java.base/java.util.concurrent=ALL-UNNAMED --add-opens java.base/java.time=ALL-UNNAMED -jar ./target/st.cbse.LogisticsCenter.client.jar








# Jakarta EE - Logistics Center - Client - Template Application

This is a template client application. It should be used as a starting point.

Run the Server with:

Maven:
```
mvn clean package
java -jar ./target/st.cbse.LogisticsCenter.client.jar
```

or in Eclipse:
```
Client.java -> Run As -> Run Application
```
