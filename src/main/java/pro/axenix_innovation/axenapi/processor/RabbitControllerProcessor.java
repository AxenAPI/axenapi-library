
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

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import pro.axenix_innovation.axenapi.code.generator.RabbitControllerCodeGenerator;
import pro.axenix_innovation.axenapi.model.kafka.KafkaHandlerData;
import pro.axenix_innovation.axenapi.model.kafka.KafkaListenerData;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes(value = {"org.springframework.amqp.rabbit.annotation.RabbitListener"})
public class RabbitControllerProcessor extends BaseControllerProcessor {
    private final String HANDLER_ANNOTATION = "org.springframework.amqp.rabbit.annotation.RabbitHandler";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        codeGenerator = new RabbitControllerCodeGenerator(processingEnv.getFiler(), properties);
    }

    @Override
    @SuppressWarnings({"java:S106", "java:S3457", "java:S3516"})
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            /* We request only one annotation, so we have only one element suitable for the annotation */
            TypeElement rabbitListenerAnnotationElement = annotations.stream().findFirst().orElse(null);
            if (rabbitListenerAnnotationElement == null) {
                return false;
            }

            /* Get all elements with the annotation. */
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(rabbitListenerAnnotationElement);

            /* Set results in list. */
            List<KafkaListenerData> listeners = new ArrayList<>();

            for (Element annotatedElement : annotatedElements) {
                if (!isAnnotatedElementSupported(annotatedElement, rabbitListenerAnnotationElement,
                        ElementKind.CLASS))
                    continue;

                if (!isPackageOk(annotatedElement))
                    continue;

                List<String> queues = Arrays.asList(annotatedElement.getAnnotation(RabbitListener.class).queues());

                if (queues.isEmpty()) {
                    messager.printMessage(
                            Diagnostic.Kind.WARNING,
                            "Annotation RabbitListener - queues are not specified: ",
                            annotatedElement
                    );
                    continue;
                }

                /* Find all methods with KafkaHandler annotation. Get DTO argument  */
                List<KafkaHandlerData> methods = getHandlerMethods((TypeElement) annotatedElement); // TODO: handlerData for Rabbit

                System.out.printf("Methods generated for Rabbit: %s\n", methods);

                listeners.add(
                        KafkaListenerData   // TODO: listenerData for Rabbit
                                .builder()
                                .listenerClassName(annotatedElement.getSimpleName().toString())
                                .topics(queues) // TODO: for now use topics prop
                                .handlers(methods)
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

    // TODO: returnedDataByMethod @ReplyTo
}
