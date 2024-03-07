
/*
 * Copyright (C) 2023 Axenix Innovations LLC
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

package pro.axenix_innovation.axenapi.code.generator;

import com.squareup.javapoet.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import pro.axenix_innovation.axenapi.consts.Constants;
import pro.axenix_innovation.axenapi.consts.KafkaGeneratorConstants;
import pro.axenix_innovation.axenapi.model.ReturnedData;
import pro.axenix_innovation.axenapi.model.kafka.KafkaHandlerData;
import pro.axenix_innovation.axenapi.model.kafka.KafkaListenerData;
import pro.axenix_innovation.axenapi.service.KafkaSenderService;
import pro.axenix_innovation.axenapi.service.RemoteMethodService;
import pro.axenix_innovation.axenapi.utils.AxenAPIProperties;
import pro.axenix_innovation.axenapi.utils.JavaPoetHelper;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class KafkaControllerCodeGenerator extends BaseControllerCodeGenerator {
    public KafkaControllerCodeGenerator(Filer filer, AxenAPIProperties properties) {
        super(filer, properties);
    }

    @Override
    protected void addControllerConstructorAndFields(TypeSpec.Builder controllerTypeBuilder, KafkaListenerData listenerData) {
        String groupId = "";
        if (listenerData.getGroupId() != null && !listenerData.getGroupId().isBlank()) {
            groupId = listenerData.getGroupId();
        }
        /* Create class fields. */
        List<FieldSpec> fields = Arrays.asList(
                JavaPoetHelper.constructField(
                        KafkaSenderService.class,
                        KafkaGeneratorConstants.KAFKA_SENDER_SERVICE_OBJECT,
                        Modifier.PRIVATE,
                        Modifier.FINAL
                ),
                JavaPoetHelper.constructField(
                        RemoteMethodService.class,
                        KafkaGeneratorConstants.MODEL_GENERATOR_SERVICE_OBJECT,
                        Modifier.PRIVATE,
                        Modifier.FINAL
                ),
                JavaPoetHelper.constructField(
                        String.class,
                        KafkaGeneratorConstants.TOPIC_OBJECT,
                        AnnotationSpec
                                .builder(Value.class)
                                .addMember("value", "$S", listenerData.getTopics().get(0))
                                .build(),
                        Modifier.PRIVATE
                ),
                JavaPoetHelper.constructField(
                        String.class,
                        KafkaGeneratorConstants.GROUP_OBJECT,
                        AnnotationSpec
                                .builder(Value.class)
                                .addMember("value", "$S", groupId)
                                .build(),
                        Modifier.PRIVATE
                )
        );

        /* Create constructor. */
        MethodSpec constructorMethodSpec = MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(KafkaSenderService.class, KafkaGeneratorConstants.KAFKA_SENDER_SERVICE_OBJECT)
                .addParameter(RemoteMethodService.class, KafkaGeneratorConstants.MODEL_GENERATOR_SERVICE_OBJECT)
                .addCode(
                        JavaPoetHelper.constructConstructorParameterAssignment(
                                KafkaGeneratorConstants.KAFKA_SENDER_SERVICE_OBJECT
                        )
                )
                .addCode(
                        JavaPoetHelper.constructConstructorParameterAssignment(
                                KafkaGeneratorConstants.MODEL_GENERATOR_SERVICE_OBJECT
                        )
                )
                .build();

        controllerTypeBuilder
                .addFields(fields)
                .addMethod(constructorMethodSpec);
    }

    @Override
    protected void addControllerClassAnnotations(TypeSpec.Builder controllerTypeBuilder, KafkaListenerData listenerData) {
        String groupId = "";
        String groupPrefix = "";
        if (!StringUtils.isBlank(listenerData.getGroupId()))
            groupId = listenerData.getGroupId();    // TODO: groupId determination duplicate code

        if (!groupId.isBlank())
            groupPrefix = groupId + "/";

        addControllerClassAnnotations(controllerTypeBuilder, "/kafka/" + groupPrefix + listenerData.getTopics().get(0));
    }

    @Override
    protected AnnotationSpec constructResponseAnnotation(ReturnedData returnedData) {
        String message = "";

        if (returnedData == null) {
            if(AxenAPIProperties.RUS_LOCALE.equals(properties.getLanguage())) {
                message = Constants.WITHOUT_RESPONSE_MESSAGE;
            } else {
                message = Constants.WITHOUT_RESPONSE_MESSAGE_ENG;
            }
        } else if (returnedData.getReturnedTopicName().isEmpty()) {
            if(AxenAPIProperties.RUS_LOCALE.equals(properties.getLanguage())) {
                message = String.format(
                        Constants.WITH_REPLY_TOPIC_RESPONSE_MESSAGE,
                        returnedData.getReturnedType().getSimpleClassName()
                );
            } else {
                message = String.format(
                        Constants.WITH_REPLY_TOPIC_RESPONSE_MESSAGE_ENG,
                        returnedData.getReturnedType().getSimpleClassName()
                );
            }
        } else {
            if(AxenAPIProperties.RUS_LOCALE.equals(properties.getLanguage())) {
                message = String.format(
                        Constants.WITH_FIXED_REPLY_TOPIC_RESPONSE_MESSAGE,
                        returnedData.getReturnedType().getSimpleClassName(),
                        returnedData.getReturnedTopicName()
                );
            } else {
                message = String.format(
                        Constants.WITH_FIXED_REPLY_TOPIC_RESPONSE_MESSAGE_ENG,
                        returnedData.getReturnedType().getSimpleClassName(),
                        returnedData.getReturnedTopicName()
                );
            }
        }

        return constructResponseAnnotation(message);
    }

    @Override
    protected CodeBlock constructMainHandlerMethodBody(KafkaHandlerData handler) {
        String sendToBlock = "$N.send($N, $N, $N, $N)";
        if(handler.isSecured()) {
            String tokenToParams = "String authToken = headers.get(\"" + properties.getTokenHeader().toLowerCase() + "\"); \n" +
                    "if(authToken != null) params.put(\"" + properties.getTokenHeader() + "\", authToken);\n";
            sendToBlock = tokenToParams + sendToBlock;
        }

        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

        codeBlockBuilder.addStatement(
                sendToBlock,
                KafkaGeneratorConstants.KAFKA_SENDER_SERVICE_OBJECT,
                KafkaGeneratorConstants.TOPIC_OBJECT,
                handler.getVariableData().getVariableName(),
                KafkaGeneratorConstants.PARAMS_OBJECT,
                KafkaGeneratorConstants.SERVLET_RESPONSE_OBJECT
        );

        if (Objects.nonNull(handler.getReturnedData())) {
            codeBlockBuilder.add(
                    JavaPoetHelper.constructFakeReturnStatement(handler.getReturnedData().getReturnedType())
            );
        }

        return codeBlockBuilder.build();
    }
}