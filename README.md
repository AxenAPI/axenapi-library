# AxenAPI

Tool for automatically creating documentation in OpenAPI 3 format for kafka and jms integrations. When building project using `annotationProcessor` for kafka and jms consumers, spring-mvc controllers will be generated, using consumer interfaces. This tool allows creating OpenAPI 3 documentation for generated controllers(using springdoc-ui, or springdoc-plugin or openapi-generator).

# Description

## Format of controllers corresponds to consumer interfaces.

For every `Listener` a controller `<ListenerClassName>Controller` will be generated.

Example of generated http method:

* POST method:
* Url: "/kafka/group-2/multiType/Subordinate"
* Returns: Subordinate

Format description:
- All generated http interfaces - are POST methods
- All generated interfaces have url, consisting of 3-4 parts:
  - first part: always starts with kafka/ - which allows to separate generated url from already existing in applicatio http interfaces.
  - second part: group - is optional. If url consists of 3 parts - that means that group is not specified.
  - third part: topic name.
  - fourth part: Name of data models being read from topic(DTO).

> 💡 Other logic does not require additional description. You can create OpenAPI specification from generated controller. Format descriptipn: https://spec.openapis.org/oas/latest.html. Authorization should be described by OpenAPI 3.* specification.



Using headers:
- To add header to message, endpoint parameters should be added, then they will be copied to kafka/jms;
- `__TypeId__` is formed automatically and should not be filled manually.
- Values of ohter headers will be filled, if they were specified with `@KafkaHandlerHeaders` or `@KafkaHandlerHeader` annotations.
- Auth token will be sent as header with name, that was specified  in `kafka.access.token.header` parameter, if annotation `@KafkaSecured` was used.



## Controller generation parameters:
Generation parameters can be specified in `axenapi.properties`. File should be stored in root directory of your project.
File example:

```
package = com.example.demo
kafka.handler.annotaion = com.example.demo.annotation.MyKafkaHandler
use.standard.kafkahandler.annotation = true
kafka.access.token.header = SERVICE_ACCESS_TOKEN
```
**Parameters description:**

| name                                 | type   | default value | description                                                                                                                                                                                                                                                                                  |
|--------------------------------------|--------|---------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| package                              | String | -             | Package, which well be processed by `annotationProcessor`. Consumers from other packages will be ignored. If not specified, all project will be scanned.                                                                                                                                     |
| kafka.handler.annotaion              | String | -             | If custom annotations are used in your consumers, to `annotationProcessor` be able to process them correctly, full name of your custon annotation should be specified. If not specified, then annotationProcessor whil use spring-kafka: `org.springframework.kafka.annotation.KafkaHandler` |
| use.standard.kafkahandler.annotation | String | true          | If `false`, then only consumers annotated with your custom annotations will be processed. Else consumers using `org.springframework.kafka.annotation.KafkaHandler` will be processed too.                                                                                                    |
| kafka.access.token.header            | String | Authorization | Name of the header, in which auth token for в Kafka/JMS will be stored.                                                                                                                                                                                                                      |
| language                             | String | eng           | Language of additional information in generated controllers. Supported values: eng, rus                                                                                                                                                                                                      | 


## Parameters influencing generated controllers in runtime:

Parameters should be specified in application.properties(or application.yml) file.

| name                            | type     | default value          | description                                                                                                                 |
|---------------------------------|----------|------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| axenapi.kafka.swagger.enabled   | boolean  | false                  | If `false`, then generated controllers will not be loaded to spring context when starting application                       |
| axenapi.headers.sendBytes       | boolean  | true                   | If `false`, then additional header with header types mapping will not be used in generated controllers                      |

## How this tool works
All generation happens when using annotationProcessor at the stage of building your project.

### Kafka
- All classes annotated with `@KafkaListener` are scanned
- if package of scanned class is different form specified in parameter `package` of `axenapi.properties` file,
  such class will be ignored
- In these classes all methods annotated with `@KafkaHandler` (and/or specified in `kafka.handler.annotaion` parameter) are scanned
- Object payload is checked. If handler has a single paramter, it would be identified as payload, if it has several parameters, then the one
  annotated with `org.springframework.messaging.handler.annotation.Payload` will be identified.
- If method has a return value, or has annotation, specifying the type of return value, then this type is being identified.
- For every listener controller code will be generated, using data received from previous steps. Generated code will use package org.axenix.axenapi.controller;
- Every method send messages to kafka to specified topic and DTO name.

### JMS
- All classes annotated with `@JmsHandler` are scanned;
- if package of scanned class is different form specified in parameter `package` of `axenapi.properties` file,
  such class will be ignored
- By `@JmsHandler.jmsTemplateName()` template `JmsTemplateRegistry` will be scanned for apropriate `JmsTemplate`
- By `@JmsHandler.destination()` massegae destination will be specified
- By `@JmsHandler.properties()` list of parametes which willbe added to `Message` will be formed
- By `@JmsHandler.description()` endpoint description will be created
- By `@JmsHandler.payload()` message type and endpoint name will be formed

When launching service `swagger` will process it, and you will be able to work with it like with regular controller.

## Installation

To install this tool to you project:
- add annotation processpr for controller generation:
  ,
  annotationProcessor "org.axenix:axenapi:{current_version}"

- if needed, add an inmplentation:

        implementation "org.axenix:axenapi:{current_version}"

- if needed, add `axenapi.properties` file. If this file doesn't exist, then all parameters will be set to default values.

### JMS

Additionally, to work with JMS you neew to register `JmsTemplate` im `JmsTemplateRegistry`
by corresponding `@JmsHandler.jmsTemplateName()`
```java
@Configuration
public class ServiceConfiguration {
   @Value("${axenapi.jmsTemplateName}")
   private String jmsTemplateName;

   @Autowired
   public void config(@Qualifier("myJmsTemplate") JmsTemplate jmsTemplate, JmsTemplateRegistry registry) {
      registry.register(jmsTemplateName, jmsTemplate);
   }

}
```

## (Optional) installing swagger-ui
For convenience it is recommended to separate documentation of your application's http interfaces and documentation of generated consumer intefaces. To achieve that you can separate your documentation in thwo groups in Swagger-ui. We recommend to use Springdoc-UI (Dependecny on `org.springdoc:springdoc-openapi-ui:<version>` should be added). Here's an example of using springdoc-ui, in which api is separated and has two different types of authenication.

```java
@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("Public-Bearer-Jwt",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER))
                        .addSecuritySchemes("Internal-Token",
                                new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("SERVICE_ACCESS_TOKEN"))
                )
                .info(new Info().title("App API").version("snapshot"));
    }
    @Bean
    GroupedOpenApi restApis() {
        return GroupedOpenApi.builder().group("kafka").pathsToMatch("/**/kafka/**").build();
    }

    @Bean
    GroupedOpenApi kafkaApis() {
        return GroupedOpenApi.builder().group("rest").pathsToMatch("/**/users/**").build();
    }
}
```

## Descripton of annotations used for creating documentaton:

### Kafka

Tool consist of following annotations:
- `@KafkaHandlerResponse` - to add return type to your controller;
- `@KafkaHandlerHeaders` - to specify headers for your Kafka messages;
  - `@KafkaHandlerHeader` - to specify a particular header;
- `@KafkaHandlerDescription` - to specify handler;
- `@KafkaHandlerTags` - to add tags for grouping methods;
- `@KafkaRemoteMethods` - to separate one method to several by method name and it's parameters;
  - `@RemoteMethod` - to specify method's name, it's tags and variables;
    - `@RemoteMethodVariable` - to specify one of the method's variables.
- `@KafkaSecured` - if authorization is required. In brakcets you can specify a securirty scheme's of OpenAPI specification name. Default value -  `Internal-Token`

Adding return value:
- If method's handler returns value, then it's type will be identified automatically, and to information, that
  return type is synchronous will be added to descriptipon, and response will be send to topic, acquired from header `replyTopic`;
- If method's handler returns value, but response is sent to particular topic, you can add anotation `@KafkaHandlerResponse`
  and specify topic's name and return value type. In this case, return value type will be of that specified in annotation;
- if method doesn't have a return value, but the response is possible in logic, you can use annotation `@KafkaHandlerResponse` and
  specify return value type, but if parameter `replyTopic` is not sepcified, then to description will be added information, that
  response will be sent to topic, recieved from `replyTopic` topic, if this parameter is specified - response will be sent to topic from annotation.

Annotation usage example:
- adding return value
```Java
   @KafkaHandlerResponse(payload = CallStatusDataDto.class)
```
- adding kafka headers:
```Java
   @KafkaHandlerHeaders(headers = {
        @KafkaHandlerHeader(header = "replyTopic", required = true)
   })
```
- adding description:
```Java
   @KafkaHandlerDescription("Обработка сообщений из топиков")
```
- adding tags:
```Java
   @KafkaHandlerTags(tags = {"call2", "call3"})
```
- adding securirty information for handler:
  - `@KafkaSecured(name = "my-securty-scheme")`

#### Separating methods - RemoteMethod
If handler's dto has parameter of `Map<String, Object>` type, wich is a set of variables,
then Swagger will not be able to identify which data is used in this field, and it should be filled manually, but
description of how this filed should be filled will not be availavle, which means that this task requires understanding of input parameters.

This approach requires addition of a different field of `String` or `Enum` types, which will be storing method names, keys,
to identify setes of variables of `Map<String, Object>` type.

To provide Swagger with information on methods and variables `RemoteMethod` was added.
It separates one method to several methods, and each has it's own specific set of variables.

To use this feature you will need to take following steps:
- Add `@KafkaHandlerRemoteMethod` annotation:
  - `methodPropertyName` - path to the input dto, in which method's name is stored
    (Path can be specified recursively using `.` symbol)

  - `methodPropertyType` - field's type (String.class - default value),
    can only be String or Enum

  - `variablesPropertyName` - path to the input dto, in which method's variables is stored
    (Path can be specified recursively using `.` symbol)

  - `methods` - list of methods, available for dto, specified by `@RemoteMethod` annotaton

- adding `@RemoteMethod` annotation:
  - `description` - method description
    (same as `@KafkaHandlerDescription` description, but for a separate method)
  - `propertyValue` - method's name, to wich it's set of variables corresponds

  - `variables` - this method's set of variables, specified by `@RemoteMethodVariable` annotation
  - `tags` - list of tags for method grouping (as example - call2, call3)

- adding `@RemoteMethodVariable` annotation:
  - `description` - variable description

  - `propertyFieldName` - String presentation of variable's key, filed name of Java format for a new dto

  - `type` - variable type, can be an object, or array of objects

For every method two additional dtos will be generated:
- First constist of methods list of veriables, fields corresponds to `propertyFieldName`, and
  every fileds has a description corresponding to `description` from `@RemoteMethodVariable`.
  Dto is named by format: `VariableBy\<MethodName\>`,
  where `MethodName` - `propertyValue` from `@RemoteMethod` in camel case.
- The second one constits of the original dto for this handler and dto with variables from the previous step.
  Name of the dto - `Execute\<OriginalDtoName\>By\<MethodName\>`, where `OriginalDtoName` - type ofthe original dto,
  and `MethodName` is as of the previous dto.
  **\[following it will reffered to as additional dto\]**

For every `@RemoteMethod` a separate endpoint will be generated, which will be accepting additional dto as input parameter.
Headers and return value is the same as the original method's.
To send messages by this endpoint it is required to fill original dto and it's set of variables and when message is sent, it will be copied and sent to Kafka.

Using such approach allows Swagger to process, display them on the ui,
correctly fill them, and controller forms an original dto, that then will be sent to Kafka.

### JMS
Path to controller has following format: `/jms/<query name>/<dto name>`\

Working with message parameters:
- added parameters to message happens by adding endpoint parameters, then `Message.setObjectProperty` is filled;
- It is possible to register `JmsMessagePostProcessor` implementatons, which will be called before sending the message.
```java
@Configuration
public class ServiceConfiguration {
   @Bean
   public JmsMessagePostProcessor replyProcessor(MqConfig config) {
      return m -> {
         try {
            m.setJMSReplyTo(new MQQueue(config.getConnectionConfigurations().get(0).getQueueManager(), config.getRsqueue()));
         } catch (JMSException ex) {
            log.error(ex.getMessage(), ex);
         }
      };
   }
}
```

## Change history
Change history is written to CHANGELOG.md