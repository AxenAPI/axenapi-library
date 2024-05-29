package pro.axenix_innovation.axenapi.utils;

import lombok.experimental.UtilityClass;

import java.util.Locale;

@UtilityClass
public class StringUtils {
    public static String snakeToCamelCase(String snakeString) {
        if (!snakeString.contains("_")) {
            return snakeString;
        }

        String camelString = snakeString.toLowerCase(Locale.ROOT);
        camelString = camelString.substring(0, 1).toUpperCase() + camelString.substring(1);
        while(camelString.contains("_")) {
            camelString = camelString.replaceFirst(
                    "_[a-z]",
                    String.valueOf(Character.toUpperCase(
                            camelString.charAt(camelString.indexOf("_") + 1))
                    )
            );
        }

        return camelString;
    }
}
