
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

package pro.axenix_innovation.axenapi.utils;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.Filer;
import javax.tools.StandardLocation;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

@Getter
public class AxenAPIProperties {
    public final static String ENG_LOCALE = "eng";
    public final static String RUS_LOCALE = "rus";
    private String packageName;
    private String annotationName;

    private String tokenHeader;

    private boolean useKafkaHandlerAnnotation = true;

    private String language;

    public static final String DEFAULT_HANDLER_VALUE = "org.springframework.kafka.annotation.KafkaHandler";

    public static final String PROPERTIES_FILE_NAME = "axenapi.properties";

    public AxenAPIProperties(Filer filer) {
        Properties props = new Properties();
        FileInputStream fileInputStream = null;

        try {
            // StandardLocation.SOURCE_PATH doesn't work
            String root = Path.of(filer.getResource(StandardLocation.CLASS_OUTPUT, "", "-")
                    .toUri())
                    .getParent().getParent().getParent().getParent().getParent().toString();
            fileInputStream = new FileInputStream(root + "/" + PROPERTIES_FILE_NAME);

            props.load(fileInputStream);
        } catch (FileNotFoundException ignored) {
            // no errors
        } catch (IOException ioException) {
            System.out.println("Failed to read file " + PROPERTIES_FILE_NAME);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        this.packageName = props.getProperty("package");
        this.annotationName = props.getProperty("kafka.handler.annotaion");
        String kafkaHandlerString = props.getProperty("use.standard.kafkahandler.annotation");
        String tokenHeader = props.getProperty("kafka.access.token.header");
        this.tokenHeader = tokenHeader == null ? "Authorization" : tokenHeader;
        this.language = props.getProperty("language");
        this.useKafkaHandlerAnnotation = kafkaHandlerString == null || Boolean.parseBoolean(kafkaHandlerString);
        if (StringUtils.isBlank(annotationName)) {
            annotationName = DEFAULT_HANDLER_VALUE;
        }

        if (StringUtils.isBlank(language)
                || !(ENG_LOCALE.equals(language) || RUS_LOCALE.equals(language))) {
            language = ENG_LOCALE;
        }
    }
}
