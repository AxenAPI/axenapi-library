package org.axenix.axenapi.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.axenix.axenapi.annotation.KafkaHandlerDescription;
import org.axenix.axenapi.annotation.KafkaHandlerResponse;
import org.axenix.axenapi.annotation.*;
import org.axenix.axenapi.code.generator.KafkaControllerCodeGenerator;
import org.axenix.axenapi.model.ClassData;
import org.axenix.axenapi.model.ParamsData;
import org.axenix.axenapi.model.ReturnedData;
import org.axenix.axenapi.model.VariableData;
import org.axenix.axenapi.model.kafka.KafkaHandlerData;
import org.axenix.axenapi.model.kafka.KafkaListenerData;
import org.axenix.axenapi.model.kafka.remote.HandlerRemoteMethodMetadata;
import org.axenix.axenapi.model.kafka.remote.RemoteMethodMetadata;
import org.axenix.axenapi.model.kafka.remote.RemoteMethodVariableMetadata;
import org.axenix.axenapi.utils.ElementHelper;
import org.axenix.axenapi.utils.AxenAPIProperties;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.util.CollectionUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.stream.Collectors;

@SupportedAnnotationTypes(value = {"org.springframework.kafka.annotation.KafkaListener"})
@Slf4j
public class KafkaControllerProcessor extends AbstractProcessor {
    private Messager messager;
    private KafkaControllerCodeGenerator codeGenerator;
    private ElementHelper helper;
    private AxenAPIProperties properties;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        messager = processingEnv.getMessager();
        helper = new ElementHelper(processingEnv);
        properties = new AxenAPIProperties(processingEnv.getFiler());
        codeGenerator = new KafkaControllerCodeGenerator(processingEnv.getFiler(), properties);
    }

    @Override
    @SuppressWarnings({"java:S106", "java:S3457", "java:S3516"})
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            /* Т.к. мы запрашиваем только одну аннотация, то в annotations будет только один элемент,
             *  соответствующий поддерживаемой аннотации. */
            TypeElement kafkaListenerAnnotationElement = annotations.stream().findFirst().orElse(null);

            if (kafkaListenerAnnotationElement == null) {
                return false;
            }

            /* Пройдем по всем элементам, использующим данную аннотацию. */
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(kafkaListenerAnnotationElement);

            /* Собираем все результаты анализа в список. */
            List<KafkaListenerData> listeners = new ArrayList<>();

            for (Element annotatedElement : annotatedElements) {
                if (Objects.equals(annotatedElement.getKind(), ElementKind.METHOD)) {
                    /* Методы пока не поддерживаются. */
                    messager.printMessage(
                            Diagnostic.Kind.WARNING,
                            "Annotation KafkaListener does not support methods",
                            annotatedElement
                    );
                } else if (!Objects.equals(annotatedElement.getKind(), ElementKind.CLASS)) {
                    /* Остальные элементы, кроме класса, не могу содержать данную аннотацию. */
                    messager.printMessage(
                            Diagnostic.Kind.WARNING,
                            "Annotation KafkaListener does not support this statement: " +
                                    annotatedElement.getKind().name(),
                            annotatedElement
                    );
                } else {
                    /* Далее работаем с классом, содержащим аннотацию KafkaListener. */
                    /* Работаем с классом в конкретном пакете (или вложенном в него), если тот указан в параметрах */
                    String packageName = properties.getPackageName();
                    if (packageName != null && !packageName.isBlank()
                            && !annotatedElement.asType().toString().startsWith(packageName)) {
                        continue;
                    }

                    /* Получим список топиков. */
                    List<String> topics = Arrays.asList(annotatedElement.getAnnotation(KafkaListener.class).topics());

                    if (topics.isEmpty()) {
                        messager.printMessage(
                            Diagnostic.Kind.WARNING,
                            "Annotation KafkaListener does not contains topics: ",
                            annotatedElement
                        );

                        continue;
                    }

                    /* Найдем все методы, которые имеют аннотацию KafkaHandler, содержат параметры и
                     * один из параметров похож на дто. */
                    List<KafkaHandlerData> methods = processingEnv.getElementUtils()
                        .getAllMembers((TypeElement) annotatedElement)
                        .stream()
                        .filter(element -> Objects.equals(element.getKind(), ElementKind.METHOD))
                        .filter(element -> {
                            List<? extends AnnotationMirror> list = this.processingEnv.getElementUtils().getAllAnnotationMirrors(element);
                            boolean isHandler = false;
                            for (AnnotationMirror annotationMirror : list) {
                                log.debug(annotationMirror.toString());
                                isHandler = checkAnnotationName(annotationMirror.toString());
                                if(isHandler) break;
                            }
                            return isHandler;
                        })
                        .map(ExecutableElement.class::cast)
                        .filter(helper::existsDtoInParameters)
                        .map(this::handlerDataByMethod)
                        .collect(Collectors.toList());

                    System.out.printf("Methods generated for kafka: %s\n", methods);

                    /* Чтение группы. */
                    String groupId = annotatedElement.getAnnotation(KafkaListener.class).groupId();

                    listeners.add(
                        KafkaListenerData
                            .builder()
                            .listenerClassName(annotatedElement.getSimpleName().toString())
                            .topics(topics)
                            .handlers(methods)
                            .groupId(groupId)
                            .build()
                    );
                }
            }

            codeGenerator.writeFile(listeners);

            return false;
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }

        return false;
    }

    private boolean checkAnnotationName(String annotation) {
        boolean contains = annotation.contains(properties.getAnnotationName());
        if(properties.isUseKafkaHandlerAnnotation()) {
            return contains || annotation.contains(AxenAPIProperties.DEFAULT_HANDLER_VALUE);
        }
        return contains;
    }

    private KafkaHandlerData handlerDataByMethod(ExecutableElement method) {
        HandlerRemoteMethodMetadata remoteMethodMetadata = extractHandlerRemoteMethodMetadata(method);

        List<String> requiredFields = new ArrayList<>();

        if(remoteMethodMetadata != null) {
            if(remoteMethodMetadata.getMethodPropertyName() != null) {
                requiredFields.add(remoteMethodMetadata.getMethodPropertyName());
            }
            if(remoteMethodMetadata.getVariablesPropertyName() != null) {
                requiredFields.add(remoteMethodMetadata.getVariablesPropertyName());
            }
        }

        VariableData variableData = variableDataByMethod(
            method,
            requiredFields
        );

        return KafkaHandlerData
                .builder()
                .secured(isSecured(method) != null)
                .securityScheme(isSecured(method))
                .description(descriptionByMethod(method))
                .variableData(variableData)
                .returnedData(returnedDataByMethod(method))
                .params(paramsDataByMethod(method))
                .handlerRemoteMethod(remoteMethodMetadata)
                .tags(tagsByMethod(method))
                .build();
    }

    private String descriptionByMethod(ExecutableElement method) {
        KafkaHandlerDescription handlerDescription = method.getAnnotation(KafkaHandlerDescription.class);

        if (handlerDescription == null) {
            return "";
        }

        return handlerDescription.value();
    }

    private List<String> tagsByMethod(ExecutableElement method) {
        KafkaHandlerTags handlerTags = method.getAnnotation(KafkaHandlerTags.class);

        if (handlerTags == null || handlerTags.tags() == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(handlerTags.tags());
    }

    private VariableData variableDataByMethod(ExecutableElement method, List<String> requiredFields) {
        VariableElement variableElement = helper.getPayloadVariableElement(method);
        TypeMirror typeMirror = variableElement.asType();

        for (String requiredProperty : requiredFields) {
            if(!helper.checkMirrorTypeContainsField(typeMirror, requiredProperty)) {
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    String.format(
                        "Payload does not contain required property %s!",
                        requiredProperty
                    ),
                    method
                );
            }
        }

        return VariableData.builder()
                .variableType(classDataByTypeMirror(typeMirror))
                .variableName(variableElement.getSimpleName().toString())
                .build();
    }

    private ReturnedData returnedDataByMethod(ExecutableElement method) {
        String replyTopic = "";
        /* Определяем наличие типа по аннотации. */
        TypeMirror returnedTypeMirror = helper.getAnnotationValue(
                helper.getAnnotationMirrorByAnnotation(method, KafkaHandlerResponse.class),
                "payload"
        );

        if (returnedTypeMirror == null) {
            /* Определяем наличие типа по возврату функции. */
            returnedTypeMirror = helper.getReturnedTypeMirror(method);
        } else {
            KafkaHandlerResponse annotation = method.getAnnotation(KafkaHandlerResponse.class);
            replyTopic = annotation.replayTopic();
        }

        if (returnedTypeMirror == null || returnedTypeMirror.toString().equals("java.lang.Void")) {
            return null;
        }

        return ReturnedData
                .builder()
                .returnedType(classDataByTypeMirror(returnedTypeMirror))
                .returnedTopicName(replyTopic)
                .build();
    }

    private List<ParamsData> paramsDataByMethod(ExecutableElement method) {
        /* Определяем список заголовков. */
        List<ParamsData> params;

        KafkaHandlerHeaders headersAnnotation = method.getAnnotation(KafkaHandlerHeaders.class);

        if (headersAnnotation == null) {
            params = Collections.emptyList();
        } else {
            params = Arrays.stream(headersAnnotation.headers())
                    .map(
                            kafkaHandlerHeader ->
                                    new ParamsData(kafkaHandlerHeader.header(), kafkaHandlerHeader.required())
                    )
                    .collect(Collectors.toList());
        }

        return params;
    }

    private HandlerRemoteMethodMetadata extractHandlerRemoteMethodMetadata(ExecutableElement method) {
        AnnotationMirror methodsAnnotationMirror =
                method.getAnnotationMirrors().stream()
                        .filter(annotationMirror ->
                                isSameType(annotationMirror, KafkaHandlerRemoteMethod.class)
                        )
                        .findFirst().orElse(null);

        if (Objects.isNull(methodsAnnotationMirror)) {
            return null;
        }

        String methodPropertyName = helper.findAnnotationValue(
                String.class,
                methodsAnnotationMirror,
                "methodPropertyName"
        );

        if(StringUtils.isBlank(methodPropertyName) || methodPropertyName.contains(" ")) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.WARNING,
                "Remote method property name must not be null, empty or contain whitespaces!",
                method,
                methodsAnnotationMirror
            );
        }

        String variablesPropertyName = helper.findAnnotationValue(
                String.class,
                methodsAnnotationMirror,
                "variablesPropertyName"
        );

        if(StringUtils.isBlank(variablesPropertyName) || variablesPropertyName.contains(" ")) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.WARNING,
                "Remote method variables property name must not be null, empty or contain whitespaces!",
                method,
                methodsAnnotationMirror
            );
        }

        TypeMirror typeMirror = helper.findAnnotationValue(
                TypeMirror.class,
                methodsAnnotationMirror,
                "methodPropertyType"
        );

        if(typeMirror != null && !helper.isStringOrEnumTypeMirror(typeMirror)) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.WARNING,
                "Remote method property type must be either java.lang.String or enumeration!",
                method,
                methodsAnnotationMirror
            );
        }

        ClassData methodPropertyType = classDataByTypeMirror(typeMirror);

        List<AnnotationMirror> methodAnnotations =
                helper.findListAnnotationValue(
                        AnnotationMirror.class,
                        methodsAnnotationMirror,
                        "methods"
                );

        List<RemoteMethodMetadata> remoteMethodMetadataList = methodAnnotations
                .stream()
                .filter(annotationMirror -> isSameType(annotationMirror, RemoteMethod.class))
                .map(annotationMirror -> {
                    RemoteMethodMetadata remoteMethodMetadata = extractRemoteMethodMetadata(annotationMirror);

                    /* Проверка наличия переменных для метода. */
                    if (CollectionUtils.isEmpty(remoteMethodMetadata.getVariables()) &&
                            Objects.isNull(remoteMethodMetadata.getVariablesType())
                    ) {
                        processingEnv.getMessager().printMessage(
                                Diagnostic.Kind.WARNING,
                                "Remote method variables are not declared",
                                method,
                                annotationMirror
                        );

                        return null;
                    }

                    if(methodPropertyType != null
                        && !helper.enumElementExists(typeMirror, remoteMethodMetadata.getPropertyValue())) {
                        processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            String.format("Remote method property value %s does not exists in enumeration %s",
                                remoteMethodMetadata.getPropertyValue(),
                                methodPropertyType.getQualifiedClassName()
                            ),
                            method,
                            annotationMirror
                        );
                    }

                    return remoteMethodMetadata;
                })
                /* Игнорируем методы без переменных. */
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        /* Не удалось найти хотя бы один метод. */
        if (remoteMethodMetadataList.isEmpty()) {
            return null;
        }

        return HandlerRemoteMethodMetadata
                .builder()
                .methodPropertyName(methodPropertyName)
                .methodPropertyType(methodPropertyType)
                .variablesPropertyName(variablesPropertyName)
                .methods(remoteMethodMetadataList)
                .build();
    }

    private String isSecured(ExecutableElement method) {
        KafkaSecured annotation = method.getAnnotation(KafkaSecured.class);
        if(annotation == null) {
            return null;
        }
        return annotation.name();
    }

    private RemoteMethodMetadata extractRemoteMethodMetadata(AnnotationMirror methodAnnotationMirror) {
        String description = helper.findAnnotationValue(
                String.class,
                methodAnnotationMirror,
                "description"
        );

        String propertyValue = helper.findAnnotationValue(
                String.class,
                methodAnnotationMirror,
                "propertyValue"
        );

        List<String> tags = helper.findListAnnotationValue(
                String.class,
                methodAnnotationMirror,
                "tags"
        );

        List<AnnotationMirror> methodAnnotations =
                helper.findListAnnotationValue(
                        AnnotationMirror.class,
                        methodAnnotationMirror,
                        "variables"
                );

        List<RemoteMethodVariableMetadata> remoteMethodVariableMetadataList = methodAnnotations
                .stream()
                .filter(annotationMirror -> isSameType(annotationMirror, RemoteMethodVariable.class))
                .map(this::extractRemoteMethodVariableMetadata)
                .collect(Collectors.toList());

        TypeMirror typeMirror = helper.findAnnotationValue(
                TypeMirror.class, methodAnnotationMirror, "variablesType"
        );

        return RemoteMethodMetadata
                .builder()
                .description(description)
                .propertyValue(propertyValue)
                .variables(remoteMethodVariableMetadataList)
                .variablesType(classDataByTypeMirror(typeMirror))
                .tags(tags)
                .build();
    }

    private RemoteMethodVariableMetadata extractRemoteMethodVariableMetadata(AnnotationMirror methodAnnotationMirror) {

        String propertyFieldName = helper.findAnnotationValue(
                String.class,
                methodAnnotationMirror,
                "propertyFieldName"
        );

        String description = helper.findAnnotationValue(
                String.class,
                methodAnnotationMirror,
                "description"
        );

        TypeMirror typeMirror = helper.findAnnotationValue(
                TypeMirror.class, methodAnnotationMirror, "type"
        );

        return RemoteMethodVariableMetadata
                .builder()
                .description(description)
                .propertyFieldName(propertyFieldName)
                .type(classDataByTypeMirror(typeMirror))
                .build();
    }

    private ClassData classDataByTypeMirror(TypeMirror typeMirror) {
        if (Objects.isNull(typeMirror)) {
            return null;
        }

        String simpleClassName = helper.getClassNameByTypeMirror(typeMirror);
        String packageName = helper.getPackageNameByTypeMirror(typeMirror);
        String qualifiedClassName = helper.getQualifiedClassNameByTypeMirror(typeMirror);

        return ClassData.builder()
                .simpleClassName(simpleClassName)
                .packageName(packageName)
                .qualifiedClassName(qualifiedClassName)
                .isArray(helper.isArrayTypeMirror(typeMirror))
                .build();
    }

    private boolean isSameType(AnnotationMirror annotationMirror, Class<?> clazz) {
        return isSameType(annotationMirror.getAnnotationType(), clazz);
    }

    private boolean isSameType(TypeMirror typeMirror, Class<?> clazz) {
        return processingEnv.getTypeUtils()
                .isSameType(
                        typeMirror,
                        processingEnv.getElementUtils()
                                .getTypeElement(clazz.getName())
                                .asType()
        );
    }


}
