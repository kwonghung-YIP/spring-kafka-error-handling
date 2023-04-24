To run the simple-producer and simple-consumer with maven
```bash
mvn clean install && mvn spring-boot:run -pl simple-consumer
mvn clean install && mvn spring-boot:run -pl simple-producer
```

```bash
mvn clean install && mvn jib:build -pl simple-consumer && jib:build -pl simple-producer
```