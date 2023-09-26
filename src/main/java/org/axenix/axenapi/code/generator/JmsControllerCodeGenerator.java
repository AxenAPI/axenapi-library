
/*
 * Copyright [yyyy] [name of copyright owner]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.axenix.axenapi.code.generator;

import com.squareup.javapoet.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.axenix.axenapi.consts.Info;
import org.axenix.axenapi.model.jms.JmsHandlerMetadata;
import org.axenix.axenapi.model.jms.PropertyMetadata;
import org.axenix.axenapi.service.JmsSenderService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.PropertyResolver;
import org.springframework.web.bind.annotation.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JmsControllerCodeGenerator {
    private final Filer filer;
    private static final String DESTINATION = "{:DESTINATION}";
    private static final String PAYLOAD_KEY = "{:PAYLOAD_KEY}";

    private static final String SEND_FUNCTION_NAME = "send" + PAYLOAD_KEY;
    @SuppressWarnings("java:S1075")
    private static final String SEND_PATH = "/send/" + DESTINATION + "/" + PAYLOAD_KEY;

    private static final String JMS_SENDER_OBJECT = "jmsService";
    private static final String PROPERTY_RESOLVER_OBJECT = "propertyResolver";

    @SuppressWarnings({"java:S112", "java:S106", "java:S3457"})
    public void writeFile(List<JmsHandlerMetadata> handlers) throws Exception {
        FieldSpec serviceFiledSpec = FieldSpec
                .builder(JmsSenderService.class, JMS_SENDER_OBJECT, Modifier.PRIVATE, Modifier.FINAL)
                .build();
        FieldSpec propertyResolverSpec = FieldSpec
                .builder(PropertyResolver.class, PROPERTY_RESOLVER_OBJECT, Modifier.PRIVATE, Modifier.FINAL)
                .build();

        MethodSpec constructorMethodSpec = MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(JmsSenderService.class, JMS_SENDER_OBJECT)
                .addParameter(PropertyResolver.class, PROPERTY_RESOLVER_OBJECT)
                .addStatement("this.$N = $N", JMS_SENDER_OBJECT, JMS_SENDER_OBJECT)
                .addStatement("this.$N = $N", PROPERTY_RESOLVER_OBJECT, PROPERTY_RESOLVER_OBJECT)
                .build();

        AnnotationSpec requestMappingSpec = AnnotationSpec
                .builder(RequestMapping.class)
                .addMember("path", "$S", "/jms")
                .build();

        AnnotationSpec conditionalOnPropertySpec = AnnotationSpec
                .builder(ConditionalOnProperty.class)
                .addMember("prefix", "$S", "swagger4kafka")
                .addMember("name", "$S", "enabled")
                .addMember("havingValue", "$S","true")
                .build();

        System.out.println("Methods generated for jms =========================");

        handlers.forEach(h ->
            System.out.printf("JmsTemplateName: %s,\npayload: %s,\ndestination: %s,\ndescription,\nproperties: %s\n",
                h.getJmsTemplateName(),
                h.getPayload(),
                h.getDestination(),
                h.getDescription(),
                h.getProperties()
            )
        );

        System.out.println("=====================================================");

        TypeSpec controllerTypeSpec = TypeSpec
                .classBuilder("JmsController")
                .addModifiers(Modifier.PUBLIC)
                .addField(serviceFiledSpec)
                .addField(propertyResolverSpec)
                .addMethod(constructorMethodSpec)
                .addMethods(methods(handlers))
                .addAnnotation(RestController.class)
                .addAnnotation(requestMappingSpec)
                .addAnnotation(conditionalOnPropertySpec)
                .build();

        JavaFile javaFile = JavaFile
                .builder(Info.CONTROLLER_PACKAGE, controllerTypeSpec)
                .indent("    ")
                .build();

        JavaFileObject builderFile = filer.createSourceFile(Info.CONTROLLER_PACKAGE + ".JmsController");

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            javaFile.writeTo(out);
        }
    }

    private List<MethodSpec> methods(List<JmsHandlerMetadata> handlers) {
        return handlers
                .stream()
                .map(this::methodByHandler)
                .collect(Collectors.toList());
    }

    private MethodSpec methodByHandler(JmsHandlerMetadata handler) {
        final String functionName = functionNameByChannelType(handler);
        final String path = pathByChannelType(handler);

        /* Create annotation list for method */
        List<AnnotationSpec> methodAnnotations = new ArrayList<>();

        AnnotationSpec annotationSpec = AnnotationSpec
                .builder(PostMapping.class)
                .addMember("path", "$S", path)
                .build();
        methodAnnotations.add(annotationSpec);

        AnnotationSpec.Builder operationAnnotationBuilder = AnnotationSpec
                .builder(Operation.class)
                .addMember("description", "$S", handler.getDescription());

        /* Add annotation with request params. */
        if (!handler.getProperties().isEmpty()) {

            List<AnnotationSpec> apiImplicitParam = annotationsByProperties(handler.getProperties());

            apiImplicitParam.forEach(propertyAnnotation -> operationAnnotationBuilder.addMember(
                    "parameters",
                    "$L",
                    propertyAnnotation
            ));
        }

        methodAnnotations.add(operationAnnotationBuilder.build());

        /* Create method arguments. */
        ParameterSpec payloadParameter = ParameterSpec
                .builder(
                        ClassName.get(
                                handler.getPayload().getPackageName(),
                                handler.getPayload().getSimpleClassName()
                        ),
                        "payload"
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

        /* Create method body. */
        final String sendToBlock = "$N.send(jmsTemplateName, payload, targetName, params)";
        final String channelName = replaceSymbols(handler.getDestination());

        final String jmsTemplateName = replaceSymbols(handler.getJmsTemplateName());

        CodeBlock codeBlock = CodeBlock
                .builder()
                .addStatement(
                    "final $T jmsTemplateName = $N.getProperty($S, $S)",
                    String.class,
                    PROPERTY_RESOLVER_OBJECT,
                    jmsTemplateName,
                    jmsTemplateName
                )
                .addStatement(
                        "final String targetName = $N.getProperty($S, $S)",
                        PROPERTY_RESOLVER_OBJECT,
                        channelName,
                        channelName
                )
                .addStatement(sendToBlock, JMS_SENDER_OBJECT)
                .build();

        return MethodSpec
                .methodBuilder(functionName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotations(methodAnnotations)
                .addParameter(payloadParameter)
                .addParameter(paramsParameter)
                .addCode(codeBlock)
                .build();
    }

    private String functionNameByChannelType(JmsHandlerMetadata handler) {
        return SEND_FUNCTION_NAME
                        .replace(PAYLOAD_KEY, handler.getPayload().getSimpleClassName());
    }

    private String pathByChannelType(JmsHandlerMetadata handler) {
        return SEND_PATH
                        .replace(DESTINATION, handler.getDestination())
                        .replace(PAYLOAD_KEY, handler.getPayload().getSimpleClassName());
    }

    private List<AnnotationSpec> annotationsByProperties(List<PropertyMetadata> properties) {
        return properties.stream()
                .map(property -> AnnotationSpec
                    .builder(Parameter.class)
                    .addMember("name", "$S", property.getName())
                    .addMember("in", "$L", "io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY")
                    .addMember("required", "$L", property.getRequired())
                    .build()
                )
                .collect(Collectors.toList());
    }

    private String replaceSymbols(String value) {
        return value.replace("$", "").replace("{", "").replace("}", "");
    }
}
