package pro.axenix_innovation.axenapi.annotation;

import lombok.Getter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated type is used as outgoing message in JMS.
 * <p> The type should also be annotated with {@link io.swagger.v3.oas.annotations.media.Schema @Schema}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Outgoing {
    /**
     * Topics for which the type is used as outgoing message.
     * @return topics
     */
    String[] topics();

    /**
     * A list of tags.
     * @return tags
     */
    String[] tags() default {"outgoing"};

    /**
     * Type of outgoing communication.
     * @return type
     */
    Type type();

    @Getter
    public static enum Type {
        EVENT("Event"), MESSAGE("Message");

        final private String text;

        Type(String text) {
            this.text = text;
        }
    }
}
