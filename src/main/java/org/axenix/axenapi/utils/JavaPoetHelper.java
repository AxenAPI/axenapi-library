
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

package org.axenix.axenapi.utils;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axenix.axenapi.model.ClassData;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JavaPoetHelper {
    private static final String CONSTRUCTOR_PARAMETER_ASSIGNMENT_STATEMENT = "this.$N = $N";

    public static FieldSpec constructField(ClassData type, String objectName, Modifier... modifiers) {
        return FieldSpec
                .builder(
                        typeNameByClassData(type),
                        objectName,
                        modifiers
                )
                .build();
    }

    public static FieldSpec constructField(ClassData type, String objectName, AnnotationSpec annotation, Modifier... modifiers) {
        return FieldSpec
                .builder(
                        typeNameByClassData(type),
                        objectName,
                        modifiers
                )
                .addAnnotation(annotation)
                .build();
    }

    public static FieldSpec constructField(ClassData type, String objectName, List<AnnotationSpec> annotations, Modifier... modifiers) {
        return FieldSpec
                .builder(
                        typeNameByClassData(type),
                        objectName,
                        modifiers
                )
                .addAnnotations(annotations)
                .build();
    }

    public static FieldSpec constructField(Class<?> type, String objectName, Modifier... modifiers) {
        return FieldSpec
                .builder(
                        type,
                        objectName,
                        modifiers
                )
                .build();
    }

    public static FieldSpec constructField(Class<?> type, String objectName, AnnotationSpec annotation, Modifier... modifiers) {
        return FieldSpec
                .builder(
                        type,
                        objectName,
                        modifiers
                )
                .addAnnotation(annotation)
                .build();
    }

    public static FieldSpec constructField(Class<?> type, String objectName, List<AnnotationSpec> annotations, Modifier... modifiers) {
        return FieldSpec
                .builder(
                        type,
                        objectName,
                        modifiers
                )
                .addAnnotations(annotations)
                .build();
    }

    public static CodeBlock constructConstructorParameterAssignment(String objectName) {
        return CodeBlock
                .builder()
                .addStatement(
                        CONSTRUCTOR_PARAMETER_ASSIGNMENT_STATEMENT,
                        objectName,
                        objectName
                )
                .build();
    }

    public static JavaFile constructDefaultDtoJavaFile(
            String className,
            String packageName,
            List<FieldSpec> fields
    ) {
        TypeSpec typeSpec = TypeSpec
                .classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotations(constructDefaultDtoAnnotations())
                .addFields(fields)
                .build();

        return JavaFile
                .builder(packageName, typeSpec)
                .indent("    ")
                .build();
    }

    public static JavaFile constructDefaultDtoJavaFile(
            String className,
            String packageName,
            List<FieldSpec> fields,
            AnnotationSpec additionalAnnotation
    ) {
        List<AnnotationSpec> annotations = new ArrayList<>(constructDefaultDtoAnnotations());
        annotations.add(additionalAnnotation);

        TypeSpec typeSpec = TypeSpec
                .classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotations(annotations)
                .addFields(fields)
                .build();

        return JavaFile
                .builder(packageName, typeSpec)
                .indent("    ")
                .build();
    }

    public static CodeBlock constructFakeReturnStatement(ClassData type) {
        return CodeBlock
                .builder()
                .addStatement(
                        "return new $T()",
                        typeNameByClassData(type)
                )
                .build();
    }

    public static TypeName typeNameByClassData(ClassData type) {
        if (type.isArray()) {
            return ArrayTypeName.of(
                    ClassName.get(
                            type.getPackageName(),
                            type.getSimpleClassName()
                    )
            );
        }

        return ClassName.get(
                type.getPackageName(),
                type.getSimpleClassName()
        );
    }

    public static AnnotationSpec constructApiModelAnnotation(String description) {
        return AnnotationSpec.builder(Schema.class)
                .addMember(
                        "description",
                        "$S",
                        description
                )
                .build();
    }

    public static AnnotationSpec constructApiModelPropertyAnnotation(String description, boolean required) {
        return AnnotationSpec.builder(Schema.class)
                .addMember(
                        "value",
                        "$S",
                        description
                )
                .addMember("required", "$L", required)
                .build();
    }

    private static List<AnnotationSpec> constructDefaultDtoAnnotations() {
        return Arrays.asList(
                AnnotationSpec
                        .builder(Data.class)
                        .build(),
                AnnotationSpec
                        .builder(Builder.class)
                        .build(),
                AnnotationSpec
                        .builder(NoArgsConstructor.class)
                        .build(),
                AnnotationSpec
                        .builder(AllArgsConstructor.class)
                        .build()
        );
    }
}
