
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

import org.springframework.messaging.handler.annotation.Payload;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.stream.Collectors;

public class ElementHelper {
    private final ProcessingEnvironment processingEnvironment;

    public ElementHelper(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    public boolean existsDtoInParameters(ExecutableElement executableElement) {
        List<? extends VariableElement> variableElements = executableElement.getParameters();
        if (variableElements.isEmpty()) {
            /* check if method has arguments */
            return false;
        }

        /* if method has one param, this param is payload */
        if (variableElements.size() == 1) return true;

        /* return true, if method hs @Payload param */
        return variableElements
                .stream()
                .anyMatch(this::isPayload);
    }

    public boolean isPayload(VariableElement variableElement) {
        return Objects.nonNull(variableElement.getAnnotation(Payload.class));
    }

    public VariableElement getPayloadVariableElement(ExecutableElement method) {
        List<? extends VariableElement> parameters = method.getParameters();

        if (parameters.size() == 1) return parameters.get(0);

        return parameters
                .stream()
                .filter(this::isPayload)
                .findFirst().orElse(null);
    }

    public boolean isArrayTypeMirror(TypeMirror typeMirror) {
        return (typeMirror.getKind() == TypeKind.ARRAY);
    }

    public boolean isStringOrEnumTypeMirror(TypeMirror typeMirror) {
        TypeElement typeElement = (TypeElement) processingEnvironment.getTypeUtils().asElement(typeMirror);
        return (typeElement.getKind() == ElementKind.ENUM || typeElement.getSimpleName().toString().equals("String"));
    }

    public String getClassNameByTypeMirror(TypeMirror typeMirror) {
        TypeMirror currentTypeMirror = typeMirror;

        if (isArrayTypeMirror(typeMirror)) {
            currentTypeMirror = ((ArrayType)typeMirror).getComponentType();
        }

        TypeElement typeElement = (TypeElement) processingEnvironment.getTypeUtils().asElement(currentTypeMirror);
        return typeElement.getSimpleName().toString();
    }

    public String getPackageNameByTypeMirror(TypeMirror typeMirror) {
        TypeMirror currentTypeMirror = typeMirror;

        if (isArrayTypeMirror(typeMirror)) {
            currentTypeMirror = ((ArrayType)typeMirror).getComponentType();
        }

        TypeElement typeElement = (TypeElement) processingEnvironment.getTypeUtils().asElement(currentTypeMirror);

        List<String> packageElements = new ArrayList<>();

        Element element = typeElement.getEnclosingElement();

        while (element instanceof TypeElement) {
            packageElements.add(element.getSimpleName().toString());
            element = element.getEnclosingElement();
        }

        packageElements.add(((PackageElement) element).getQualifiedName().toString());
        Collections.reverse(packageElements);

        return String.join(".", packageElements);
    }

    public String getQualifiedClassNameByTypeMirror(TypeMirror typeMirror) {
        TypeMirror currentTypeMirror = typeMirror;

        if (isArrayTypeMirror(typeMirror)) {
            currentTypeMirror = ((ArrayType)typeMirror).getComponentType();
        }

        TypeElement typeElement = (TypeElement)processingEnvironment.getTypeUtils().asElement(currentTypeMirror);
        return typeElement.getQualifiedName().toString();
    }

    public TypeMirror getReturnedTypeMirror(ExecutableElement method) {
        /* check if method has return type */
        TypeMirror returnedTypeMirror = method.getReturnType();

        if (returnedTypeMirror instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) returnedTypeMirror;

            List<? extends TypeMirror> arguments = declaredType.getTypeArguments();

            if (arguments.isEmpty()) {
                return declaredType;
            } else if (arguments.size() == 1) {     // wrapper type with a single type argument
                return arguments.get(0);
            }
        }

        return null;
    }

    public AnnotationMirror getAnnotationMirrorByAnnotation(Element typeElement, Class<?> annotationClass) {
        List<? extends AnnotationMirror> annotationMirrors = typeElement.getAnnotationMirrors();

        for(AnnotationMirror annotationMirror: annotationMirrors) {
            String key = annotationMirror.getAnnotationType().toString();
            String simpleName = annotationClass.getName();

            if (Objects.equals(key, simpleName)) {
                return annotationMirror;
            }
        }

        return null;
    }

    public TypeMirror getAnnotationValue(AnnotationMirror annotationMirror, String key) {
        if (annotationMirror == null) {
            return null;
        }

        Set<? extends Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>> entries =
                annotationMirror.getElementValues().entrySet();

        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : entries) {
            String annotationKey = entry.getKey().getSimpleName().toString();
            if (Objects.equals(annotationKey, key)) {
                return (TypeMirror) entry.getValue().getValue();
            }
        }

        return null;
    }

    public <T> T findAnnotationValue(Class<T> clazz, AnnotationMirror annotationMirror, String key) {
        Object value = annotationMirror.getElementValues().entrySet()
                .stream()
                .filter(entry -> Objects.equals(key,  entry.getKey().getSimpleName().toString()))
                .map(entry -> entry.getValue().getValue())
                .findFirst()
                .orElse(null);

        if (Objects.isNull(value)) {
            return null;
        }

        return clazz.cast(value);
    }

    public <T> List<T> findListAnnotationValue(Class<T> clazz, AnnotationMirror annotationMirror, String key) {
        List<AnnotationValue> values = annotationMirror.getElementValues()
                .entrySet()
                .stream()
                .filter(entry -> Objects.equals(key,  entry.getKey().getSimpleName().toString()))
                .map(entry -> (List<AnnotationValue>)entry.getValue().getValue())
                .findFirst()
                .orElse(null);

        if (Objects.isNull(values)) {
            return Collections.emptyList();
        }

        return values.stream()
                .map(AnnotationValue::getValue)
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    public boolean enumElementExists(TypeMirror enumTypeMirror, String enumElement) {
        TypeElement typeElement = (TypeElement)processingEnvironment.getTypeUtils().asElement(enumTypeMirror);

        if(typeElement.getKind() != ElementKind.ENUM) {
            return true;
        }

        return typeElement.getEnclosedElements().stream()
            .filter(element -> element.getKind() == ElementKind.ENUM_CONSTANT)
            .anyMatch(element -> element.getSimpleName().toString().equals(enumElement));
    }

    public boolean checkMirrorTypeContainsField(TypeMirror typeMirror, String requiredProperty) {
        TypeElement typeElement = (TypeElement)processingEnvironment.getTypeUtils().asElement(typeMirror);
        List<String> fieldPathList = Arrays.stream(requiredProperty.split("\\."))
            .collect(Collectors.toList());

        String fieldPath = fieldPathList.get(0);

        Optional<? extends Element> nestedElement = typeElement.getEnclosedElements().stream()
            .filter(element -> element.getKind() == ElementKind.FIELD)
            .filter(element -> element.getSimpleName().toString().equals(fieldPath))
            .findFirst();

        if(nestedElement.isEmpty()) {
            return false;
        }

        if (fieldPathList.size() == 1) {
            return true;
        }

        fieldPathList.remove(0);
        String nextAbsoluteFieldPath = String.join(".", fieldPathList);

        return checkMirrorTypeContainsField(nestedElement.get().asType(), nextAbsoluteFieldPath);
    }
}
