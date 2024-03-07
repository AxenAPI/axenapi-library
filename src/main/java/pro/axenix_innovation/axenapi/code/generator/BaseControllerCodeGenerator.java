package pro.axenix_innovation.axenapi.code.generator;

import com.squareup.javapoet.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pro.axenix_innovation.axenapi.consts.Info;
import pro.axenix_innovation.axenapi.consts.KafkaGeneratorConstants;
import pro.axenix_innovation.axenapi.model.ClassData;
import pro.axenix_innovation.axenapi.model.JavaFileMetadata;
import pro.axenix_innovation.axenapi.model.ParamsData;
import pro.axenix_innovation.axenapi.model.ReturnedData;
import pro.axenix_innovation.axenapi.model.kafka.EndpointAnnotationsMetadata;
import pro.axenix_innovation.axenapi.model.kafka.KafkaHandlerData;
import pro.axenix_innovation.axenapi.model.kafka.KafkaListenerData;
import pro.axenix_innovation.axenapi.model.kafka.remote.RemoteMethodMetadata;
import pro.axenix_innovation.axenapi.utils.AxenAPIProperties;
import pro.axenix_innovation.axenapi.utils.JavaPoetHelper;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class BaseControllerCodeGenerator {
    protected final Filer filer;
    protected final AxenAPIProperties properties;

    @SuppressWarnings("java:S112")
    public void writeFile(List<KafkaListenerData> listeners) throws Exception {
        /* Form auxiliary DTOs for the remote call functionality. */
        List<JavaFileMetadata> javaFiles = listeners.stream()
                .map(KafkaListenerData::getHandlers)
                .flatMap(List::stream)
                .filter(handlerData -> Objects.nonNull(handlerData.getHandlerRemoteMethod()))
                .map(this::constructRemoteMethodListModels)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        /* Create controllers. */
        listeners.stream()
                .map(this::constructListenerController)
                .forEach(javaFiles::add);

        /* Save created files. */
        for (JavaFileMetadata javaFileMetadata : javaFiles) {
            JavaFileObject builderFile = filer.createSourceFile(qualifiedClassName(javaFileMetadata));

            try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                javaFileMetadata.getJavaFile().writeTo(out);
            }
        }
    }

    protected JavaFileMetadata constructListenerController(KafkaListenerData listenerData) {
        /* Create class */
        var controllerTypeBuilder = TypeSpec
                .classBuilder(listenerData.getListenerClassName() + "Controller")
                .addModifiers(Modifier.PUBLIC);

        addControllerConstructorAndFields(controllerTypeBuilder, listenerData);

        controllerTypeBuilder.addMethods(
                        constructControllerEndpoints(listenerData.getHandlers())
                );

        addControllerClassAnnotations(controllerTypeBuilder, listenerData);

        TypeSpec controllerTypeSpec = controllerTypeBuilder.build();

        JavaFile javaFile = JavaFile
                .builder(Info.CONTROLLER_PACKAGE, controllerTypeSpec)
                .indent("    ")
                .build();

        return JavaFileMetadata
                .builder()
                .className(listenerData.getListenerClassName() + "Controller")
                .packageName(Info.CONTROLLER_PACKAGE)
                .javaFile(javaFile)
                .build();
    }

    protected abstract void addControllerConstructorAndFields(TypeSpec.Builder controllerTypeBuilder, KafkaListenerData listenerData);

    protected abstract void addControllerClassAnnotations(TypeSpec.Builder controllerTypeBuilder, KafkaListenerData listenerData);

    protected void addControllerClassAnnotations(TypeSpec.Builder controllerTypeBuilder, String path) {
        AnnotationSpec restControllerSpec = AnnotationSpec
                .builder(RestController.class)
                .build();

        AnnotationSpec requestAnnotationSpec = AnnotationSpec
                .builder(RequestMapping.class)
                .addMember("path", "$S", path)
                .build();

        /*AnnotationSpec conditionalOnPropertySpec = AnnotationSpec
                .builder(ConditionalOnBean.class)
                .addMember("value", "$T.class", AxenaAPIConfiguration.class)
                .build(); TODO: Not used for now */

        controllerTypeBuilder
                .addAnnotation(restControllerSpec)
                .addAnnotation(requestAnnotationSpec);
//                .addAnnotation(conditionalOnPropertySpec); TODO: Not used for now
    }

    protected List<MethodSpec> constructControllerEndpoints(List<KafkaHandlerData> handlers) {
        return handlers
                .stream()
                .map(this::constructControllerEndpointsByHandler)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    protected List<MethodSpec> constructControllerEndpointsByHandler(KafkaHandlerData handler) {
        MethodSpec mainMethod = constructMainHandlerMethod(handler);

        if (Objects.isNull(handler.getHandlerRemoteMethod())) {
            return Collections.singletonList(mainMethod);
        }

        List<MethodSpec> methodSpecList = new ArrayList<>();
        methodSpecList.add(mainMethod);
        methodSpecList.addAll(constructRemoteMethods(handler));

        return methodSpecList;
    }

    protected MethodSpec constructMainHandlerMethod(KafkaHandlerData handler) {
        final String functionName = "execute" + handler.getVariableData().getVariableType().getSimpleClassName();
        final String path =  "/" + handler.getVariableData().getVariableType().getSimpleClassName();

        /* Create list of annotations for method. */
        List<AnnotationSpec> methodAnnotations = constructEndpointAnnotations(
                EndpointAnnotationsMetadata.builder()
                        .description(handler.getDescription())
                        .params(handler.getParams())
                        .returnedData(handler.getReturnedData())
                        .tags(handler.getTags())
                        .secured(handler.isSecured())
                        .securityScheme(handler.getSecurityScheme())
                        .build(),
                path
        );

        /* Create method params (request params). */
        ParameterSpec payloadParameter = ParameterSpec
                .builder(
                        ClassName.get(
                                handler.getVariableData().getVariableType().getPackageName(),
                                handler.getVariableData().getVariableType().getSimpleClassName()
                        ),
                        handler.getVariableData().getVariableName()
                )
                .addAnnotation(RequestBody.class)
                .build();

        AnnotationSpec.Builder hiddenBuilder =  AnnotationSpec
                .builder(Parameter.class)
                .addMember("hidden", "$L", true);

        ParameterSpec paramsParameter = ParameterSpec
                .builder(ParameterizedTypeName.get(Map.class, String.class, String.class), "params")
                .addAnnotation(RequestParam.class)
                .addAnnotation(hiddenBuilder.build())
                .build();

        ParameterSpec responseParameter = ParameterSpec
                .builder(HttpServletResponse.class, KafkaGeneratorConstants.SERVLET_RESPONSE_OBJECT) // TODO: use base class for constants
                .build();

        ParameterSpec headers = ParameterSpec
                .builder(ParameterizedTypeName.get(Map.class, String.class, String.class), KafkaGeneratorConstants.HEADERS_PROPERTY_VALUE) // TODO: use base class for constants
                .addAnnotation(RequestHeader.class)
                .build();


        CodeBlock codeBlock = constructMainHandlerMethodBody(handler);

        /* Create return value of method. */
        TypeName returnedType = Objects.isNull(handler.getReturnedData()) ?
                TypeName.VOID : JavaPoetHelper.typeNameByClassData(handler.getReturnedData().getReturnedType());

        return MethodSpec
                .methodBuilder(functionName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotations(methodAnnotations)
                .addParameter(payloadParameter)
                .addParameter(paramsParameter)
                .addParameter(responseParameter)
                .addParameter(headers)
                .addCode(codeBlock)
                .returns(returnedType)
                .build();
    }

    protected abstract CodeBlock constructMainHandlerMethodBody(KafkaHandlerData handler);

    protected List<MethodSpec> constructRemoteMethods(KafkaHandlerData handler) {
        return handler.getHandlerRemoteMethod().getMethods()
                .stream()
                .map(method -> constructRemoteMethod(handler, method))
                .collect(Collectors.toList());
    }

    protected MethodSpec constructRemoteMethod(
            KafkaHandlerData handler,
            RemoteMethodMetadata method
    ) {
        final String methodPropertyValue = snakeToCamelCase(method.getPropertyValue());
        final String className = generateSupportDtoClassName(
                handler.getVariableData().getVariableType().getSimpleClassName(),
                methodPropertyValue
        );
        final String functionName = "execute" + className;
        final String path =  "/" + className;

        /* Create annotation lists of method. */
        List<AnnotationSpec> methodAnnotations = constructEndpointAnnotations(
                EndpointAnnotationsMetadata.builder()
                        .description(method.getDescription())
                        .params(handler.getParams())
                        .returnedData(handler.getReturnedData())
                        .tags(method.getTags())
                        .build(),
                path
        );

        /* Create method arguments. */
        ParameterSpec payloadParameter = ParameterSpec
                .builder(
                        ClassName.get(
                                Info.DTO_PACKAGE,
                                generateSupportDtoClassName(
                                        handler.getVariableData().getVariableType().getSimpleClassName(),
                                        methodPropertyValue
                                )
                        ),
                        KafkaGeneratorConstants.GENERATED_DTO_OBJECT
                )
                .addAnnotation(RequestBody.class)
                .build();

        AnnotationSpec.Builder hiddenBuilder =  AnnotationSpec
                .builder(Parameter.class)
                .addMember("hidden", "$L", true);

        ParameterSpec paramsParameter = ParameterSpec
                .builder(ParameterizedTypeName.get(Map.class, String.class, String.class), "params")
                .addAnnotation(RequestParam.class)
                .addAnnotation(hiddenBuilder.build())
                .build();

        ParameterSpec responseParameter = ParameterSpec
                .builder(HttpServletResponse.class, KafkaGeneratorConstants.SERVLET_RESPONSE_OBJECT)
                .build();

        /* Form method body. */
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

        /* form structure of DTO. */
        codeBlockBuilder.add(
                constructLocalVariable(
                        handler.getVariableData().getVariableType(),
                        KafkaGeneratorConstants.ORIGINAL_DTO_OBJECT,
                        KafkaGeneratorConstants.GENERATED_DTO_OBJECT,
                        KafkaGeneratorConstants.GET_ORIGINAL_DTO_METHOD
                ));

        ClassData variableType;

        if (Objects.isNull(method.getVariablesType())) {
            variableType = ClassData.builder()
                    .simpleClassName(
                            generateVariableDtoClassName(
                                    methodPropertyValue
                            ))
                    .packageName(Info.DTO_PACKAGE)
                    .build();
        } else {
            variableType = method.getVariablesType();
        }

        codeBlockBuilder.add(
                constructLocalVariable(
                        variableType,
                        KafkaGeneratorConstants.VARIABLES_OBJECT,
                        KafkaGeneratorConstants.GENERATED_DTO_OBJECT,
                        KafkaGeneratorConstants.GET_DATA_DTO_METHOD
                ));

        codeBlockBuilder.add(
                constructMethodPropertyVariable(
                        method.getPropertyValue(),
                        handler.getHandlerRemoteMethod().getMethodPropertyType()
                )
        );

        /* Forming the final structure of original DTO. */
        final String injectPropertyBlock = "$N.replaceMethodAndVariables($N, $S, $N, $S, $N)";

        codeBlockBuilder.addStatement(
                injectPropertyBlock,
                KafkaGeneratorConstants.MODEL_GENERATOR_SERVICE_OBJECT,
                KafkaGeneratorConstants.ORIGINAL_DTO_OBJECT,
                handler.getHandlerRemoteMethod().getMethodPropertyName(),
                KafkaGeneratorConstants.METHOD_PROPERTY_VALUE,
                handler.getHandlerRemoteMethod().getVariablesPropertyName(),
                KafkaGeneratorConstants.VARIABLES_OBJECT
        );

        /* send message to kafka. */
        final String sendToBlock = "$N.send($N, $N.getOriginalDto(), $N, $N)";

        codeBlockBuilder.addStatement(
                sendToBlock,
                KafkaGeneratorConstants.KAFKA_SENDER_SERVICE_OBJECT,
                KafkaGeneratorConstants.TOPIC_OBJECT,
                KafkaGeneratorConstants.GENERATED_DTO_OBJECT,
                KafkaGeneratorConstants.PARAMS_OBJECT,
                KafkaGeneratorConstants.SERVLET_RESPONSE_OBJECT
        );

        if (Objects.nonNull(handler.getReturnedData())) {
            codeBlockBuilder.add(
                    JavaPoetHelper.constructFakeReturnStatement(handler.getReturnedData().getReturnedType())
            );
        }

        CodeBlock codeBlock = codeBlockBuilder.build();

        /* Create the return value of method. */
        TypeName returnedType = Objects.isNull(handler.getReturnedData()) ?
                TypeName.VOID : JavaPoetHelper.typeNameByClassData(handler.getReturnedData().getReturnedType());

        return MethodSpec
                .methodBuilder(functionName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotations(methodAnnotations)
                .addParameter(payloadParameter)
                .addParameter(paramsParameter)
                .addParameter(responseParameter)
                .addCode(codeBlock)
                .returns(returnedType)
                .addException(Exception.class)
                .build();
    }

    protected CodeBlock constructMethodPropertyVariable(String name, ClassData type) {
        ClassName className = ClassName.get(
                type.getPackageName(),
                type.getSimpleClassName()
        );

        if (type.getSimpleClassName().equals("String")) {
            return CodeBlock
                    .builder()
                    .addStatement(
                            "$T $N = $S",
                            className,
                            KafkaGeneratorConstants.METHOD_PROPERTY_VALUE,
                            name
                    )
                    .build();
        }



        return CodeBlock
                .builder()
                .addStatement(
                        "$T $N = $T.$N",
                        className,
                        KafkaGeneratorConstants.METHOD_PROPERTY_VALUE,
                        className,
                        name
                )
                .build();
    }

    protected CodeBlock constructLocalVariable(
            ClassData type,
            String localVariableName,
            String innerDtoName,
            String innerDtoGetMethodName
    ) {
        final String getVariableBlock = "$T $N = $N.$N()";

        ClassName className = ClassName.get(
                type.getPackageName(),
                type.getSimpleClassName()
        );

        return CodeBlock
                .builder()
                .addStatement(
                        getVariableBlock,
                        className,
                        localVariableName,
                        innerDtoName,
                        innerDtoGetMethodName
                )
                .build();
    }

    protected List<AnnotationSpec> constructEndpointAnnotations(EndpointAnnotationsMetadata endpointMetadata, String path) {
        List<AnnotationSpec> methodAnnotations = new ArrayList<>();

        AnnotationSpec annotationSpec = AnnotationSpec
                .builder(PostMapping.class)
                .addMember("path", "$S", path)
                .build();
        methodAnnotations.add(annotationSpec);

        /* Create annotation with method description. */
        AnnotationSpec.Builder operationAnnotationBuilder =  AnnotationSpec
                .builder(Operation.class)
                .addMember("description", "$S", endpointMetadata.getDescription());

        /* Add tags. */
        if (!endpointMetadata.getTags().isEmpty()) {
            CodeBlock codeBlock = endpointMetadata.getTags()
                    .stream()
                    .map(tag -> CodeBlock.of("$S", tag))
                    .collect(CodeBlock.joining(",", "{", "}"));

            operationAnnotationBuilder.addMember("tags", codeBlock);
        }


        /* Add annotation with params. */
        if (!endpointMetadata.getParams().isEmpty()) {

            List<AnnotationSpec> apiImplicitParam = constructAnnotationsByParams(endpointMetadata.getParams());

            apiImplicitParam.forEach(propertyAnnotation -> operationAnnotationBuilder.addMember(
                    "parameters",
                    "$L",
                    propertyAnnotation
            ));
        }

        methodAnnotations.add(operationAnnotationBuilder.build());
        methodAnnotations.add(constructResponseAnnotation(endpointMetadata.getReturnedData()));

        /* Add security annotation. */
        if(endpointMetadata.isSecured()) {
            AnnotationSpec.Builder openApiSecurityBuilder =  AnnotationSpec
                    .builder(SecurityRequirement.class)
                    .addMember("name", "$S", endpointMetadata.getSecurityScheme());
            methodAnnotations.add(openApiSecurityBuilder.build());
        }
        return methodAnnotations;
    }

    protected String generateSupportDtoClassName(String originalModelName, String methodPropertyValue) {
        return originalModelName + "By" + methodPropertyValue;
    }

    protected List<AnnotationSpec> constructAnnotationsByParams(List<ParamsData> params) {
        return params.stream()
                .map(param -> AnnotationSpec
                        .builder(Parameter.class)
                        .addMember("name", "$S", param.getName())
                        .addMember("in", "$L", "io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY")
                        .addMember("schema", "$L", AnnotationSpec.builder(Schema.class)
                                .addMember("implementation", "$L", "String.class")
                                .build())
                        .addMember("required", "$L", param.getRequired())
                        .build()
                )
                .collect(Collectors.toList());
    }

    protected abstract AnnotationSpec constructResponseAnnotation(ReturnedData returnedData);
    protected AnnotationSpec constructResponseAnnotation(String description) {
        AnnotationSpec.Builder responseAnnotationSpec = AnnotationSpec
                .builder(ApiResponse.class);

        responseAnnotationSpec.addMember("responseCode", "$S", "200");
        responseAnnotationSpec.addMember("description", "$S", description);

        return responseAnnotationSpec.build();
    }

    protected List<JavaFileMetadata> constructRemoteMethodListModels(KafkaHandlerData handlerData) {
        return handlerData.getHandlerRemoteMethod().getMethods()
                .stream()
                .map(method -> constructRemoteMethodModels(
                        handlerData,
                        method
                ))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    protected List<JavaFileMetadata> constructRemoteMethodModels(KafkaHandlerData kafkaHandlerData, RemoteMethodMetadata method) {
        /* Create DTO with method variables. */
        JavaFileMetadata variableDto = null;
        ClassData variableType;

        /* Generate DTO with variables, if type is not set */
        if (Objects.isNull(method.getVariablesType())) {
            variableDto = constructRemoteMethodVariableDto(method);
            variableType = ClassData.builder()
                    .simpleClassName(variableDto.getClassName())
                    .packageName(variableDto.getPackageName())
                    .build();
        } else {
            variableType = method.getVariablesType();
        }

        /* Create additional DTO with original DTO and variables. */
        JavaFileMetadata remoteMethodSupportDto = constructRemoteMethodSupportDto(
                kafkaHandlerData,
                method,
                variableType
        );

        if (Objects.isNull(variableDto)) {
            return Collections.singletonList(remoteMethodSupportDto);
        }

        return Arrays.asList(variableDto, remoteMethodSupportDto);
    }

    protected JavaFileMetadata constructRemoteMethodVariableDto(RemoteMethodMetadata method) {
        final String className = generateVariableDtoClassName(
                snakeToCamelCase(method.getPropertyValue())
        );

        List<FieldSpec> fields = method.getVariables()
                .stream()
                .map(variable -> JavaPoetHelper.constructField(
                        variable.getType(),
                        variable.getPropertyFieldName(),
                        JavaPoetHelper.constructApiModelPropertyAnnotation(variable.getDescription(), false),
                        Modifier.PRIVATE
                ))
                .collect(Collectors.toList());

        JavaFile javaFile = JavaPoetHelper.constructDefaultDtoJavaFile(
                className,
                Info.DTO_PACKAGE,
                fields,
                JavaPoetHelper.constructApiModelAnnotation(
                        String.format(AxenAPIProperties.RUS_LOCALE.equals(properties.getLanguage()) ?
                                "Параметры метода %s" :
                                "Method parameters %s", method.getPropertyValue())
                )
        );

        return JavaFileMetadata
                .builder()
                .className(className)
                .packageName(Info.DTO_PACKAGE)
                .javaFile(javaFile)
                .build();
    }

    protected JavaFileMetadata constructRemoteMethodSupportDto(
            KafkaHandlerData kafkaHandlerData,
            RemoteMethodMetadata method,
            ClassData variable
    ) {
        String simpleClassName = kafkaHandlerData.getVariableData().getVariableType().getSimpleClassName();

        final String className = generateSupportDtoClassName(
                simpleClassName,
                snakeToCamelCase(method.getPropertyValue())
        );

        List<FieldSpec> fields = Arrays.asList(
                JavaPoetHelper.constructField(
                        kafkaHandlerData.getVariableData().getVariableType(),
                        KafkaGeneratorConstants.ORIGINAL_DTO_OBJECT,
                        Arrays.asList(
                                AnnotationSpec.builder(Valid.class)
                                        .build(),
                                AnnotationSpec.builder(NotNull.class)
                                        .build(),
                                JavaPoetHelper.constructApiModelPropertyAnnotation(
                                        AxenAPIProperties.RUS_LOCALE.equals(properties.getLanguage()) ?
                                                "Исходное дто" : "Original DTO",
                                        true
                                )
                        ),
                        Modifier.PRIVATE
                ),
                JavaPoetHelper.constructField(
                        variable,
                        KafkaGeneratorConstants.VARIABLES_OBJECT,
                        Arrays.asList(
                                AnnotationSpec.builder(Valid.class)
                                        .build(),
                                AnnotationSpec.builder(NotNull.class)
                                        .build(),
                                JavaPoetHelper.constructApiModelPropertyAnnotation(
                                        String.format(AxenAPIProperties.RUS_LOCALE.equals(properties.getLanguage()) ?
                                                "Параметры метода %s" :
                                                "Method parameters %s", method.getPropertyValue()),
                                        true
                                )
                        ),
                        Modifier.PRIVATE
                )
        );

        JavaFile javaFile = JavaPoetHelper.constructDefaultDtoJavaFile(
                className,
                Info.DTO_PACKAGE,
                fields,
                JavaPoetHelper.constructApiModelAnnotation(
                        String.format("Обертка для метода с типом %s", simpleClassName)
                )
        );

        return JavaFileMetadata
                .builder()
                .className(className)
                .packageName(Info.DTO_PACKAGE)
                .javaFile(javaFile)
                .build();
    }

    protected String generateVariableDtoClassName(String methodPropertyValue) {
        return "VariableBy" + methodPropertyValue;
    }

    protected String snakeToCamelCase(String snakeString) {
        String camelString = snakeString.toLowerCase(Locale.ROOT);
        camelString = camelString.substring(0, 1).toUpperCase() + camelString.substring(1);
        while(camelString.contains("_")) {
            camelString = camelString.replaceFirst(
                    "_[a-z]",
                    String.valueOf(Character.toUpperCase(
                            camelString.charAt(camelString.indexOf("_") + 1))
                    )
            );
        }

        return camelString;
    }

    protected String qualifiedClassName(JavaFileMetadata javaFileMetadata) {
        return javaFileMetadata.getPackageName() + "." + javaFileMetadata.getClassName();
    }
}
