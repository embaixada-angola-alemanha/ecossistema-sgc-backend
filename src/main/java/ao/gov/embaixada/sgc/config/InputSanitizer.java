package ao.gov.embaixada.sgc.config;

import java.util.regex.Pattern;

public final class InputSanitizer {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern NULL_BYTE_PATTERN = Pattern.compile("\0");
    private static final int MAX_PARAM_LENGTH = 4096;

    private InputSanitizer() {}

    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }

        String result = NULL_BYTE_PATTERN.matcher(input).replaceAll("");
        result = HTML_TAG_PATTERN.matcher(result).replaceAll("");
        result = result.trim();

        if (result.length() > MAX_PARAM_LENGTH) {
            result = result.substring(0, MAX_PARAM_LENGTH);
        }

        return result;
    }
}
