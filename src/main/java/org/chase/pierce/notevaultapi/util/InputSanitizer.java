package org.chase.pierce.notevaultapi.util;

import java.util.regex.Pattern;

public final class InputSanitizer {

    private InputSanitizer() {
    }

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    private static final Pattern DANGEROUS_TAG_PATTERN = Pattern.compile(
            "<\\s*/?(script|iframe|object|embed|form|input|link|meta|style|base|applet)[^>]*>",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern DANGEROUS_PROTOCOL_PATTERN = Pattern.compile(
            "(javascript|data|vbscript)\\s*:",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern EVENT_HANDLER_PATTERN = Pattern.compile(
            "\\bon\\w+\\s*=",
            Pattern.CASE_INSENSITIVE
    );


    public static String stripAllHtml(String input) {
        if (input == null) {
            return null;
        }
        return HTML_TAG_PATTERN.matcher(input).replaceAll("").trim();
    }

    public static String sanitizeContent(String input) {
        if (input == null) {
            return null;
        }
        String sanitized = DANGEROUS_TAG_PATTERN.matcher(input).replaceAll("");
        sanitized = EVENT_HANDLER_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = DANGEROUS_PROTOCOL_PATTERN.matcher(sanitized).replaceAll("");
        return sanitized.trim();
    }

    public static String sanitizePlainText(String input) {
        if (input == null) {
            return null;
        }
        String stripped = stripAllHtml(input);
        return stripped.isEmpty() ? null : stripped;
    }
}
