package pro.axenix_innovation.axenapi.processor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import pro.axenix_innovation.axenapi.annotation.*;
import pro.axenix_innovation.axenapi.code.generator.BaseControllerCodeGenerator;
import pro.axenix_innovation.axenapi.model.ClassData;
import pro.axenix_innovation.axenapi.model.ParamsData;
import pro.axenix_innovation.axenapi.model.ReturnedData;
import pro.axenix_innovation.axenapi.model.VariableData;
import pro.axenix_innovation.axenapi.model.kafka.KafkaHandlerData;
import pro.axenix_innovation.axenapi.model.kafka.remote.HandlerRemoteMethodMetadata;
import pro.axenix_innovation.axenapi.model.kafka.remote.RemoteMethodMetadata;
import pro.axenix_innovation.axenapi.model.kafka.remote.RemoteMethodVariableMetadata;
import pro.axenix_innovation.axenapi.utils.AxenAPIProperties;
import pro.axenix_innovation.axenapi.utils.ElementHelper;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseControllerProcessor extends AbstractProcessor {
    protected Messager messager;
    protected BaseControllerCodeGenerator codeGenerator;
    protected ElementHelper helper;
    protected AxenAPIProperties properties;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        messager = processingEnv.getMessager();
        helper = new ElementHelper(processingEnv);
        properties = new AxenAPIProperties(processingEnv.getFiler());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    protected boolean isAnnotatedElementSupported(Element annotatedElement, TypeElement kafkaListenerAnnotationElement,
                                                  ElementKind ... supportedKinds) {
        boolean isSupported = false;
        for (ElementKind kind : supportedKinds) {
            if (Objects.equals(annotatedElement.getKind(), kind)) {
                isSupported = true;
                break;
            }
        }

        if (!isSupported) {
            messager.printMessage(
                    Diagnostic.Kind.WARNING,
                    String.format("Annotation %s does not support %s",
                            kafkaListenerAnnotationElement.getSimpleName(), annotatedElement.getKind()),
                    annotatedElement
            );
        }

        return isSupported;
    }

    /**
     * Check whether the element is in the specified package (incl. nested packages)
     */
    protected boolean isPackageOk(Element annotatedElement) {
        String packageName = properties.getPackageName();
        return !(packageName != null && !packageName.isBlank()
                && !annotatedElement.asType().toString().startsWith(packageName));
    }

    protected List<KafkaHandlerData> getHandlerMethods(TypeElement typeElement) { // TODO: use base type instead of KafkaHandlerData
        return processingEnv.getElementUtils()
                .getAllMembers(typeElement)
                .stream()
                .filter(element -> Objects.equals(element.getKind(), ElementKind.METHOD))
                .filter(element -> {
                    List<? extends AnnotationMirror> list = this.processingEnv.getElementUtils().getAllAnnotationMirrors(element);
                    boolean isHandler = false;
                    for (AnnotationMirror annotationMirror : list) {
                        System.out.printf(annotationMirror.toString());
                        isHandler = checkAnnotationName(annotationMirror.toString());
                        if (isHandler) break;
                    }
                    return isHandler;
                })
                .map(ExecutableElement.class::cast)
                .filter(helper::existsDtoInParameters)
                .map(this::handlerDataByMethod)
                .collect(Collectors.toList());
    }

    protected boolean checkAnnotationName(String annotation) {
        return !StringUtils.isBlank(properties.getAnnotationName()) && annotation.contains(properties.getAnnotationName());
    }

    protected KafkaHandlerData handlerDataByMethod(ExecutableElement method) {
        HandlerRemoteMethodMetadata remoteMethodMetadata = extractHandlerRemoteMethodMetadata(method);

        List<String> requiredFields = new ArrayList<>();

        if (remoteMethodMetadata != null) {
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

        return KafkaHandlerData     // TODO: use base handlerData
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

    protected HandlerRemoteMethodMetadata extractHandlerRemoteMethodMetadata(ExecutableElement method) {
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

                    /* Check if method has arguments. */
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
                /* Ignore methods without arguments. */
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        /* if can not find any suitable method. */
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

    protected VariableData variableDataByMethod(ExecutableElement method, List<String> requiredFields) {
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

    protected String isSecured(ExecutableElement method) {
        KafkaSecured annotation = method.getAnnotation(KafkaSecured.class);  // TODO: base annotation
        if(annotation == null) {
            return null;
        }
        return annotation.name();
    }

    protected String descriptionByMethod(ExecutableElement method) {
        KafkaHandlerDescription handlerDescription = method.getAnnotation(KafkaHandlerDescription.class); // TODO: base annotation

        if (handlerDescription == null) {
            return "";
        }

        return handlerDescription.value();
    }

    protected ReturnedData returnedDataByMethod(ExecutableElement method) {
        var returnedTypeMirror = helper.getReturnedTypeMirror(method);
        if (returnedTypeMirror == null || returnedTypeMirror.toString().equals("java.lang.Void")) {
            return null;
        }

        return ReturnedData
                .builder()
                .returnedType(classDataByTypeMirror(returnedTypeMirror))
                .build();
    }

    protected List<ParamsData> paramsDataByMethod(ExecutableElement method) {
        /* get list of headers */
        List<ParamsData> params;

        KafkaHandlerHeaders headersAnnotation = method.getAnnotation(KafkaHandlerHeaders.class); // TODO: base annotation

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

    protected List<String> tagsByMethod(ExecutableElement method) {
        KafkaHandlerTags handlerTags = method.getAnnotation(KafkaHandlerTags.class); // TODO: base annotation

        if (handlerTags == null || handlerTags.tags() == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(handlerTags.tags());
    }

    protected boolean isSameType(AnnotationMirror annotationMirror, Class<?> clazz) {
        return isSameType(annotationMirror.getAnnotationType(), clazz);
    }

    protected boolean isSameType(TypeMirror typeMirror, Class<?> clazz) {
        return processingEnv.getTypeUtils()
                .isSameType(
                        typeMirror,
                        processingEnv.getElementUtils()
                                .getTypeElement(clazz.getName())
                                .asType()
                );
    }

    protected ClassData classDataByTypeMirror(TypeMirror typeMirror) {
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

    protected RemoteMethodMetadata extractRemoteMethodMetadata(AnnotationMirror methodAnnotationMirror) {
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

    protected RemoteMethodVariableMetadata extractRemoteMethodVariableMetadata(AnnotationMirror methodAnnotationMirror) {

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
}
