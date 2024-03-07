
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
import org.springframework.beans.factory.annotation.Value;
import pro.axenix_innovation.axenapi.consts.Constants;
import pro.axenix_innovation.axenapi.consts.RabbitGeneratorConstants;
import pro.axenix_innovation.axenapi.model.ReturnedData;
import pro.axenix_innovation.axenapi.model.kafka.KafkaHandlerData;
import pro.axenix_innovation.axenapi.model.kafka.KafkaListenerData;
import pro.axenix_innovation.axenapi.service.RabbitSenderService;
import pro.axenix_innovation.axenapi.utils.AxenAPIProperties;
import pro.axenix_innovation.axenapi.utils.JavaPoetHelper;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RabbitControllerCodeGenerator extends BaseControllerCodeGenerator {
    public RabbitControllerCodeGenerator(Filer filer, AxenAPIProperties properties) {
        super(filer, properties);
    }

    @Override
    protected void addControllerConstructorAndFields(TypeSpec.Builder controllerTypeBuilder, KafkaListenerData listenerData) {
        /* Create class fields. */
        List<FieldSpec> fields = Arrays.asList(
                JavaPoetHelper.constructField(
                        RabbitSenderService.class,
                        RabbitGeneratorConstants.SENDER_SERVICE_FIELD_NAME,
                        Modifier.PRIVATE,
                        Modifier.FINAL
                ),
                /* JavaPoetHelper.constructField(
                        RemoteMethodService.class,
                        KafkaGeneratorConstants.MODEL_GENERATOR_SERVICE_OBJECT,
                        Modifier.PRIVATE,
                        Modifier.FINAL
                ),  TODO: Commented for now */
                JavaPoetHelper.constructField(
                        String.class,
                        RabbitGeneratorConstants.QUEUE_FIELD_NAME,
                        AnnotationSpec
                                .builder(Value.class)
                                .addMember("value", "$S", listenerData.getTopics().get(0)) // TODO: topics prop is used temporarily
                                .build(),
                        Modifier.PRIVATE
                )
        );

        /* Create constructor. */
        MethodSpec constructorMethodSpec = MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(RabbitSenderService.class, RabbitGeneratorConstants.SENDER_SERVICE_FIELD_NAME)
//                .addParameter(RemoteMethodService.class, KafkaGeneratorConstants.MODEL_GENERATOR_SERVICE_OBJECT)  TODO: Commented for now
                .addCode(
                        JavaPoetHelper.constructConstructorParameterAssignment(
                                RabbitGeneratorConstants.SENDER_SERVICE_FIELD_NAME
                        )
                )
                /*.addCode(
                        JavaPoetHelper.constructConstructorParameterAssignment(
                                KafkaGeneratorConstants.MODEL_GENERATOR_SERVICE_OBJECT
                        )
                ) TODO: Commented for now */
                .build();

        controllerTypeBuilder
                .addFields(fields)
                .addMethod(constructorMethodSpec);
    }

    @Override
    protected void addControllerClassAnnotations(TypeSpec.Builder controllerTypeBuilder, KafkaListenerData listenerData) {
        addControllerClassAnnotations(controllerTypeBuilder, "/rabbit/" + listenerData.getTopics().get(0)); // TODO: topics prop is used temporarily
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
        }
        // TODO: return parameters not supported yet
        return constructResponseAnnotation(message);
    }

    @Override
    protected CodeBlock constructMainHandlerMethodBody(KafkaHandlerData handler) {
        String sendToBlock = "$N.send($N, $N)";
        if (handler.isSecured()) {
            String tokenToParams = "String authToken = headers.get(\"" + properties.getTokenHeader().toLowerCase() + "\"); \n" +
                    "if (authToken != null) params.put(\"" + properties.getTokenHeader() + "\", authToken);\n";
            sendToBlock = tokenToParams + sendToBlock;
        }

        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

        codeBlockBuilder.addStatement(
                sendToBlock,
                RabbitGeneratorConstants.SENDER_SERVICE_FIELD_NAME,
                RabbitGeneratorConstants.QUEUE_FIELD_NAME,
                handler.getVariableData().getVariableName()
        );

        if (Objects.nonNull(handler.getReturnedData())) {
            codeBlockBuilder.add(
                    JavaPoetHelper.constructFakeReturnStatement(handler.getReturnedData().getReturnedType())
            );
        }

        return codeBlockBuilder.build();
    }
}