
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

package pro.axenix_innovation.axenapi.processor;

import pro.axenix_innovation.axenapi.annotation.KafkaHandlerResponse;
import org.apache.commons.lang3.StringUtils;
import pro.axenix_innovation.axenapi.annotation.*;
import pro.axenix_innovation.axenapi.code.generator.KafkaControllerCodeGenerator;
import pro.axenix_innovation.axenapi.model.ReturnedData;
import pro.axenix_innovation.axenapi.model.kafka.KafkaHandlerData;
import pro.axenix_innovation.axenapi.model.kafka.KafkaListenerData;
import org.springframework.kafka.annotation.KafkaListener;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.*;

@SupportedAnnotationTypes(value = {"org.springframework.kafka.annotation.KafkaListener"})
public class KafkaControllerProcessor extends BaseControllerProcessor {
    private final String HANDLER_ANNOTATION = "org.springframework.kafka.annotation.KafkaHandler";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        codeGenerator = new KafkaControllerCodeGenerator(processingEnv.getFiler(), properties);
    }

    @Override
    @SuppressWarnings({"java:S106", "java:S3457", "java:S3516"})
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            /* We request only one annotation, so we have only one element suitable for the annotation */
            TypeElement kafkaListenerAnnotationElement = annotations.stream().findFirst().orElse(null);
            if (kafkaListenerAnnotationElement == null) {
                return false;
            }

            /* Get all elements with the annotation. */
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(kafkaListenerAnnotationElement);

            /* Set results in list. */
            List<KafkaListenerData> listeners = new ArrayList<>();

            for (Element annotatedElement : annotatedElements) {
                if (!isAnnotatedElementSupported(annotatedElement, kafkaListenerAnnotationElement,
                        ElementKind.CLASS))
                    continue;

                if (!isPackageOk(annotatedElement))
                    continue;

                /* Get list of topics. */
                List<String> topics = Arrays.asList(annotatedElement.getAnnotation(KafkaListener.class).topics());

                if (topics.isEmpty()) {
                    messager.printMessage(
                            Diagnostic.Kind.WARNING,
                            "Annotation KafkaListener does not contain topics: ",
                            annotatedElement
                    );

                    continue;
                }

                /* Find all methods with KafkaHandler annotation. Get DTO argument  */
                List<KafkaHandlerData> methods = getHandlerMethods((TypeElement) annotatedElement);

                System.out.printf("Methods generated for kafka: %s\n", methods);

                /* Get group. */
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

            codeGenerator.writeFile(listeners);

            return false;
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }

        return false;
    }

    @Override
    protected boolean checkAnnotationName(String annotation) {
        var contains = super.checkAnnotationName(annotation);
        if (properties.isUseKafkaHandlerAnnotation()) {  // TODO: base name for prop
            return contains || annotation.contains(HANDLER_ANNOTATION);
        }
        return contains;
    }

    @Override
    protected ReturnedData returnedDataByMethod(ExecutableElement method) {
        ReturnedData retData = null;
        String replyTopic = "";

        /* get type from annotation */
        TypeMirror returnedTypeMirror = helper.getAnnotationValue(
                helper.getAnnotationMirrorByAnnotation(method, KafkaHandlerResponse.class),
                "payload"
        );

        if (returnedTypeMirror == null) {
            retData = super.returnedDataByMethod(method);
        } else {
            KafkaHandlerResponse annotation = method.getAnnotation(KafkaHandlerResponse.class);
            replyTopic = annotation.replayTopic();
        }

        if ((returnedTypeMirror == null || returnedTypeMirror.toString().equals("java.lang.Void")) &&
                retData == null) {
            return null;
        }

        if (returnedTypeMirror != null) {
            retData.setReturnedType(classDataByTypeMirror(returnedTypeMirror));
        }

        retData.setReturnedTopicName(replyTopic);
        return retData;
    }
}
