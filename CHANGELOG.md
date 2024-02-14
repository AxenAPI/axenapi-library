### version 1.0.1
1) GPG signing
2) change group pro.axenix-innovation
3) change package on pro.axenix-innovation
### version 1.0.0
1) create from kafka listeners REST controllers
2) using axenapi.properties. File should be stored in root directory of your project. Parameters:package,kafka.handler.annotaion, use.standard.kafkahandler.annotation, kafka.access.token.header, language
3) use application.properties files. Properties: axenapi.kafka.swagger.enabled, axenapi.headers.sendBytes
4) add annotations:
   1) `@KafkaHandlerResponse` - to add return type to your controller;
   2) `@KafkaHandlerHeaders` - to specify headers for your Kafka messages;
   3) `@KafkaHandlerHeader` - to specify a particular header;
   4) `@KafkaHandlerDescription` - to specify handler;
   5) `@KafkaHandlerTags` - to add tags for grouping methods;
   6) `@KafkaRemoteMethods` - to separate one method to several by method name and it's parameters;
   7) `@RemoteMethod` - to specify method's name, it's tags and variables;
   8) `@RemoteMethodVariable` - to specify one of the method's variables.
   9) `@KafkaSecured` - if authorization is required. In brackets, you can specify a security scheme's of OpenAPI specification name. Default value -  `Internal-Token`
5) Generated controller will return value with appropriate type.


