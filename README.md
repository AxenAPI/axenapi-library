# AxenAPI

Библиотека предназначена для автоматического создания документации в формате OpenAPI 3 для взаимодействий по kafka и jms. При сборке проекта с помощью annotationProcessor по kafka и jms consumers генерируются spring-mvc controllers повторяющие интерфейс соответсвующих consumers. Таким обрахзом, по созданным контроллерам подно будет создать документацию в формате OpenAPI 3 (с помощью springdoc-ui, или springdoc-plugin или openapi-generator).

# Описание

## Формат url для контроллера, повторяющего интерфейс consumer

Для каждого Listener формируется свой контроллер с именем `<ListenerClassName>Controller`.

Пример сгенерированного http метода:
* Метод Post
* Url: "/kafka/group-2/multiType/Subordinate"
* Возвращает: Subordinate

Описание формата:
- все сгенерированные http интерфейсы - post методы
- все сгенерированные интерфейсы имеют url состоящий из 3-4 частей:
     - первая часть: всегда начинаются с kafka/ - позволяет отделить сгенерированные url от уже имеющихся в приложении http интерфейсов
     - вторая часть: группа - необязательная часть. Если url состоит из 3 частей, то считается, что группа не указана.
     - третья часть: наименование топика
     - четвертая часть: наименование считываемой из топика модели данных (DTO)

> :bulb: Остальная логика не нуждается в дополнительном описании. По сгенерированным контроллерам теперь есть возможность создать спецификацию в OpenAPI формате. Описание формата: https://spec.openapis.org/oas/latest.html



Работа с хедерами:
- добавление хедеров к сообщению происходит через добавление параметров для эндпойнта, далее они копируются для отправки в кафку/jms;
- `__TypeId__` формируется автоматически и его заполнять нет необходимости
- Значения других хедеров заполняются, если они были указаны с помощью аннотаций `@KafkaHandlerHeaders` и `@KafkaHandlerHeader` 
- токен авторизации будет передан в качестве хедера с наименованием указанном в параметре генерации контроллеров `kafka.access.token.header`, если указана аннотация `@KafkaSecured`


## Параметры генерации контроллеров
Параметры генерации можно указать в файле `axenapi.properties`. Файл должен находится в корне проекта. 
Пример файла:

```
package = com.example.demo
kafka.handler.annotaion = com.example.demo.annotation.MyKafkaHandler
use.standart.kafkahandler.annotation = true
kafka.access.token.header = SERVICE_ACCESS_TOKEN
```
**Описание параметров:**

| наименование | тип | значение по умодчанию | описание
| ------ | ------ | ------ | ------ |
| package | String | - | указывается пакет, в который  бцдет обрабатываться с помощью annotationProcessor. Consumers из других пакетов будут проигнорированы и по ним не будет сгенерирован spring mvc controller. Если не указано, но сканируется весь проект. 
| kafka.handler.annotaion | String | - | если в проекте для kafka consumrs используется кастомная аннотация, то чтобы annotationProcessor учитывал такие consumers надо указать в параметре полное наименование вашей кастомной аннотации. Если не указано, то annotationProcessor работат с аннотацией из spring-kafka: `org.springframework.kafka.annotation.KafkaHandler`
| use.standart.kafkahandler.annotation | String | true | если указан `false`, то будут учитывать только consumers с вашей кастомной аннотацией. Иначе, будут учитыватья и consumers с аннотацией `org.springframework.kafka.annotation.KafkaHandler`. 
| kafka.access.token.header | String | Authorization | наименования хедера, куда помещается токен авторизации при отправки в Kafka/JMS.

## Параметры влияющие на работу сгенерированных котроллеров, после запуска приложения

Параметры указываются в application.properties (или application.yml) файле.

| наименование                   | тип     | значение по умолчанию | описание                                                                                                                   |
|--------------------------------|---------|-----------------------|----------------------------------------------------------------------------------------------------------------------------|
| axenapi.kafka.swagger.enabled  | boolean | false                 | если указан false, то сгенерированные контроллеры не будут подгружаться в spring context во время запуска приложения       
| axenapi.headers.sendBytes      | boolean | true                  | если указан false, то при использовании контроллеров не будет отправляться доплнительный header с маппиногом типов headers 

## Алгоритм работы
Весь процесс выполняется во время сборки сервиса с помощью annotationProcessor на этапе сборке проекта. 

### Kafka
- определяются все классы с аннотациями `@KafkaListener` 
- если пакет найденного класса отличается от указанного в `axenapi.properties` в свойстве `package`, 
  то такой класс игнорируется
- в этих классах определяются все методы с аннотацией `@KafkaHandler` (и/или указанной в параметре `kafka.handler.annotaion`)
- проверяется наличие payload объекта. Если у хэндлера только один параметр, то он воспринимается как payload, если несколько, то ищется тот,
  что помечен аннотацией `org.springframework.messaging.handler.annotation.Payload`
- если метод содержит возвращаемое значение, либо имеется аннотация, описывающая тип возвращаемого значение, то тип запоминается;
- для каждого листенера по полученным данным генерируется код контроллера в пакете org.axenix.axenapi.controller;
- каждый метод контроллера реализует отправку сообщения в кафку по определенному топику и названию DTO.

### JMS
- определяются все классы с аннотациями `@JmsHandler`;
- если пакет найденного класса отличается от указанного в `axenapi.properties` в свойстве `package`,
  то такой класс игнорируется
- по `@JmsHandler.jmsTemplateName()` будет произведен поиск в `JmsTemplateRegistry` подходящего `JmsTemplate`
- по `@JmsHandler.destination()` определяется то, куда будет отправлено сообщение
- по `@JmsHandler.properties()` будет сформирован список параметров, что будут добавлены в `Message`
- по `@JmsHandler.description()` будет создано описание для эндпоинта
- по `@JmsHandler.payload()` сформировано название эндпоинта и тип сообщения

При запуске сервиса сваггер подхватывает сгенерированные контроллеры и с ними можно работать как с обычным контроллером.

## Подключение к проекту

Для добавления библиотеки в проект необходимо:
- добавить процессор аннотаций для генерации кода контроллера:
,
        annotationProcessor "org.axenix:axenapi:{current_version}" 

- при необходимости использования аннотаций добавить имплементацию:

        implementation "org.axenix:axenapi:{current_version}"

- при необходимости можно добавить файл `axenapi.properties`. Если файла нет, то всем параметрам генерации кода укажется значение по умодчанию.

### JMS

Дополнительно для работы через JMS необходимо зарегистрировать `JmsTemplate` в `JmsTemplateRegistry` 
по соответствующему значению `@JmsHandler.jmsTemplateName()`
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

## (Опционально) Подключение swagger-ui
Для удобства работы рекомендуется разделить документации http интерфейсов вашего приложения и документацию http интерфейсов сгенерированных по consumers. Для этого ваше api можно раздели на две группы в Swagger-ui. Мы рекомендуем исользовать Springdoc-UI (нужно добавить зависимость от библиотеки `org.springdoc:springdoc-openapi-ui:<version>`). Далее следует пример подключения springdoc-ui, в котором api разделено на две группы и есть два вида аутентификации. 

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

## Описание аннотаций для создания документации

### Kafka

Библиотека предоставляет следующие аннотации:
- `@KafkaHandlerResponse` - для добавления в контроллер типа возвращаемого значения;
- `@KafkaHandlerHeaders` - для описания списка хедеров для отправки в кафку;
  - `@KafkaHandlerHeader` - для описания конкретного хедера для отправки в кафку;
- `@KafkaHandlerDescription` - для добавления описания хендлера;
- `@KafkaHandlerTags` - для добавления меток группировки методов;
- `@KafkaRemoteMethods` - для разделения одного метода на несколько в зависимости от названия метода 
и списка его переменных;
  - `@RemoteMethod` - для описания наименования метода, его меток и списка переменных для этого метода;
    - `@RemoteMethodVariable` - для описания одной переменной метода.
- `@KafkaSecured` - если требуется авторизация. В скобках можно указать наименование securirty scheme в OpenAPI спецификации. Значение по умолчанию -   `Internal-Token`

Добавление возвращаемого значения:
- если метод хандлера возвращает значение, то тип автоматически подхватится, а в описание добавится информация,
  что возвращаемое значение синхронное, а ответ отправляется в топик, полученный из хедера `replyTopic`;
- если метод хандлера возвращает значение, но ответ приходит в конкретный топик, то можно добавить аннотацию `@KafkaHandlerResponse`
  и прописать название топика и тип возвращаемого значения, в этом случае тип возвращаемого значения будет заменен на тип из аннотации;
- если метод не возвращает значение, но в логике ответ предусмотрен, то можно добавить аннотацию `@KafkaHandlerResponse` определив
  тип возвращаемого значения, при этом, если не указывать параметр `replyTopic`, то в описание добавится
  информация, что ответ возвращается в топик, полученный из хедера `replyTopic`, а если указать - то в топик из аннотации.

Примеры добавления аннотаций:
- добавление возвращаемого значения
```Java
   @KafkaHandlerResponse(payload = CallStatusDataDto.class)
```
- добавление заголовков для кафки:
```Java
   @KafkaHandlerHeaders(headers = {
        @KafkaHandlerHeader(header = "replyTopic", required = true)
   })
```
- добавление описания:
```Java
   @KafkaHandlerDescription("Обработка сообщений из топиков")
```
- добавление меток:
```Java
   @KafkaHandlerTags(tags = {"call2", "call3"})
```
- Добавить информацию о securirty для handler:
  - `@KafkaSecured(name = "my-securty-scheme")`

#### Разделение на методы - RemoteMethod
Если во входном дто у хендлера одно из полей типа `Map<String, Object>`, что является набором переменных,
то Swagger не сможет понять какие данные требуются в этом поле и их придется заполнять самостоятельно, но
описание того как заполнять будет недоступно, поэтому эта задача требует понимания того, что подается на вход.

Такой подход требует наличия другого поля типа `String` или `Enum`, что является названием метода, ключом, 
по которому определялся бы набор переменных типа `Map<String, Object>`.

Чтобы предоставить Swagger'у информацию о методах и их переменных добавлена возможность `RemoteMethod`. 
Она как бы разбивает один метода на несколько, каждый из которых имеет свой ряд переменных.

Добавление этой возможности происходит в несколько этапов:
- добавление аннотации `@KafkaHandlerRemoteMethod`:
  - `methodPropertyName` - путь до поля входного дто, в котором лежит наименование метода 
  (Путь может задаваться рекурсивно с использованием символа `.`)
  
  - `methodPropertyType` - тип этого поля (String.class - значение по-умолчанию), 
  может содержать только тип String или Enum
  
  - `variablesPropertyName` - путь до поля входного дто, в котором лежат переменные метода
    (Путь может задаваться рекурсивно с использованием символа `.`)
  
  - `methods` - список методов, допустимых для данного дто, задается аннотацией `@RemoteMethod`
  
- добавление аннотации `@RemoteMethod`:
  - `description` - описание метода 
  (Аналогично описанию `@KafkaHandlerDescription` только для отдельного метода)
  - `propertyValue` - название метода, по которому соответствует свой набор переменных
  
  - `variables` - набор переменных для этого метода, задается аннотацией `@RemoteMethodVariable`
  - `tags` - список меток для группировки методов (Например, call2, call3)
  
- добавление аннотации `@RemoteMethodVariable`:
  - `description` - описание переменной
  
  - `propertyFieldName` - строковое представление ключа переменной, имя поля в формате Java для новой дто
  
  - `type` - тип переменной, может быть объектом класса, или массивом объектов классов

Для каждого метода генерируется два вспомогательных дто:
 - первое содержит список переменных для этого метода, поля соответствуют `propertyFieldName`, а
каждое поле имеет описание в соответствии с `description` из `@RemoteMethodVariable`. 
Наименование дто формируется следующим образом: `VariableBy\<MethodName\>`,
где `MethodName` - `propertyValue` из `@RemoteMethod` в camel case
 - второе содержит оригинальное дто для этого хендлера и дто с переменными из предыдущего пункта, 
название дто - `Execute\<OriginalDtoName\>By\<MethodName\>`, где `OriginalDtoName` - тип оригинального дто, 
а `MethodName` как для предыдущего дто 
**\[В дальнейшем будет называться вспомогательным дто\]**

Для каждого `@RemoteMethod` генерируется отдельный эндпойнт, который на вход принимает вспомогательное дто.
Хедеры и выходное значение дублируется как для основного метода. 
Для отправки сообщения через такой эндпойнт необходимо заполнить оригинальное дто и набор переменных и
при отправке название метода и набор переменных копируется в оригинальное дто и отправляется в кафку.

Использование такого подхода позволяет Swagger'у считывать такие переменные, отображать их на ui, 
корректно заполнять, а формированием оригинального дто занимается контроллер, 
который и отправляет сообщения в кафку.

### JMS
Путь контроллера имеет вид `/jms/<название очереди>/<Название дто>`\

Работа с параметрами сообщения:
- добавление параметров к сообщению происходит через добавление параметров для эндпойнта, далее на их основе заполняется `Message.setObjectProperty`;
- есть возможность регистрировать реализации `JmsMessagePostProcessor` интерфейса, что будут вызваны перед отправкой сообщения
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

## История изменений
Представлена в файле CHANGELOG.md
