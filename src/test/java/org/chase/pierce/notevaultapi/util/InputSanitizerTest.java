package org.chase.pierce.notevaultapi.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class InputSanitizerTest {

    @ParameterizedTest
    @NullSource
    void testNullStripAllHtml(String input) {
        assertNull(InputSanitizer.stripAllHtml(input));
    }

    @ParameterizedTest(name = "\"{0}\" → \"{1}\"")
    @MethodSource("stripAllHtmlCases")
    void testRemovesHtmlTags(String input, String expected) {
        assertEquals(expected, InputSanitizer.stripAllHtml(input));
    }

    static Stream<Arguments> stripAllHtmlCases() {
        return Stream.of(
                Arguments.of("<b>Hello</b> <i>World</i>", "Hello World"),
                Arguments.of("<script>alert('xss')</script>", "alert('xss')"),
                Arguments.of("<div><p>Nested</p></div>", "Nested"),
                Arguments.of("<a href=\"url\">Link</a>", "Link"),
                Arguments.of("plain text", "plain text"),
                Arguments.of("", ""),
                Arguments.of("no tags at all", "no tags at all"),
                Arguments.of("<br/><hr/>", "")
        );
    }

    @ParameterizedTest
    @NullSource
    void testNullSanitizeContent(String input) {
        assertNull(InputSanitizer.sanitizeContent(input));
    }

    @ParameterizedTest(name = "removes dangerous: \"{0}\" → \"{1}\"")
    @MethodSource("dangerousContentCases")
    void testRemovesDangerousContent(String input, String expected) {
        assertEquals(expected, InputSanitizer.sanitizeContent(input));
    }

    static Stream<Arguments> dangerousContentCases() {
        return Stream.of(
                // Script tags
                Arguments.of("<script>alert('xss')</script>", "alert('xss')"),
                Arguments.of("<SCRIPT>alert('xss')</SCRIPT>", "alert('xss')"),
                Arguments.of("<Script>alert('xss')</Script>", "alert('xss')"),
                // Iframe tags
                Arguments.of("<iframe src=\"evil.com\"></iframe>", ""),
                // Object and embed tags
                Arguments.of("<object>content</object>", "content"),
                Arguments.of("<embed>content</embed>", "content"),
                // Form and input tags
                Arguments.of("<form action=\"evil.com\"><input type=\"hidden\"></form>", ""),
                // Event handlers
                Arguments.of("<img src=\"pic.jpg\" onerror=\"alert('xss')\">", "<img src=\"pic.jpg\" >"),
                Arguments.of("<div onclick=\"steal()\">click</div>", "<div >click</div>"),
                Arguments.of("<body onload=\"malware()\">", "<body >"),
                // Dangerous protocols
                Arguments.of("<a href=\"javascript:alert('xss')\">Click</a>", "Click"),
                Arguments.of("<a href=\"data:text/html,<script>alert(1)</script>\">Click</a>", "Click"),
                Arguments.of("<a href=\"vbscript:run()\">Click</a>", "Click")
        );
    }

    @ParameterizedTest(name = "preserves safe: \"{0}\"")
    @MethodSource("safeContentCases")
    void testPreservesSafeContent(String input) {
        assertEquals(input, InputSanitizer.sanitizeContent(input));
    }

    static Stream<String> safeContentCases() {
        return Stream.of(
                "<p>Hello <b>World</b></p>",
                "<a href=\"https://example.com\">Link</a>",
                "<ul><li>Item 1</li><li>Item 2</li></ul>",
                "<h1>Title</h1>",
                "<blockquote>Quote</blockquote>",
                "Just plain text"
        );
    }

    @ParameterizedTest
    @NullSource
    void testNullSanitizePlainText(String input) {
        assertNull(InputSanitizer.sanitizePlainText(input));
    }

    @ParameterizedTest(name = "\"{0}\" → null")
    @MethodSource("plainTextNullCases")
    void testEmptySanitizePlainText(String input) {
        assertNull(InputSanitizer.sanitizePlainText(input));
    }

    static Stream<String> plainTextNullCases() {
        return Stream.of(
                "<b></b>",
                "<div></div>",
                "<br/>"
        );
    }

    @ParameterizedTest(name = "\"{0}\" → \"{1}\"")
    @MethodSource("plainTextCases")
    void testStripsHtml(String input, String expected) {
        assertEquals(expected, InputSanitizer.sanitizePlainText(input));
    }

    static Stream<Arguments> plainTextCases() {
        return Stream.of(
                Arguments.of("<b>Hello</b>", "Hello"),
                Arguments.of("My Note Title", "My Note Title"),
                Arguments.of("<script>alert('xss')</script>My Note", "alert('xss')My Note"),
                Arguments.of("  <i>trimmed</i>  ", "trimmed"),
                Arguments.of("<div><p>Nested Content</p></div>", "Nested Content")
        );
    }
}
