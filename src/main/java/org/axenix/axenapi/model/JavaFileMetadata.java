package org.axenix.axenapi.model;

import com.squareup.javapoet.JavaFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JavaFileMetadata {
    private String className;
    private String packageName;
    private JavaFile javaFile;
}
