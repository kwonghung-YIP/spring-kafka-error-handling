### Objective

### Features covered in ths repo:

* Define the DefaultErrorHandler bean for handling error raised by receiver.
* Set up the retry mechanism with FixedFallBack policy.
* Define DeadLetterPublishingRecoverer bean to forward the problem message to dead letter topic.
* Handle the RuntimeException raised in receiver.
* Handle the DeserializationException and avoid blocking the topic.
* Validate the payload passed into @KafkaListener function with @Payload and @Valid annotations.
* Serialize and deserialize Kafka messages with AVRO and Kafka Schema Registry.
* Configure multiple modules maven project for sharing the AVRO pojo to both producer and consumer.
* Build Docker-compose with single-node kafka, ksql-server, kafka schema registry and kafka-UI management console for local development.
* Run multiple consumer and consumer group with Kubernetes. 
* Configure skaffold and kustomize for deploying into the local microk8s.

### Modules enable for microk8s
* dns
* registry
* storage
* ingress
* dns
* cert-manager

### Useful commands

To run the simple-producer and simple-consumer with maven
```bash
mvn clean install && mvn spring-boot:run -pl simple-consumer
mvn clean install && mvn spring-boot:run -pl simple-producer
```

```bash
mvn clean install && mvn jib:build -pl simple-consumer && jib:build -pl simple-producer
```