package ao.gov.embaixada.sgc.config;

import ao.gov.embaixada.commons.security.filter.InputSanitizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputSanitizerTest {

    @Test
    void sanitize_shouldReturnNullForNullInput() {
        assertNull(InputSanitizer.sanitize(null));
    }

    @Test
    void sanitize_shouldStripHtmlTags() {
        assertEquals("alert('xss')", InputSanitizer.sanitize("<script>alert('xss')</script>"));
        assertEquals("Hello World", InputSanitizer.sanitize("<b>Hello</b> <i>World</i>"));
        assertEquals("text", InputSanitizer.sanitize("<div class=\"foo\">text</div>"));
    }

    @Test
    void sanitize_shouldRemoveNullBytes() {
        assertEquals("hello", InputSanitizer.sanitize("hel\0lo"));
        assertEquals("test", InputSanitizer.sanitize("\0test\0"));
    }

    @Test
    void sanitize_shouldTrimWhitespace() {
        assertEquals("hello", InputSanitizer.sanitize("  hello  "));
        assertEquals("hello world", InputSanitizer.sanitize("\t hello world \n"));
    }

    @Test
    void sanitize_shouldTruncateLongInput() {
        String longInput = "a".repeat(5000);
        String result = InputSanitizer.sanitize(longInput);
        assertEquals(4096, result.length());
    }

    @Test
    void sanitize_shouldHandleEmptyString() {
        assertEquals("", InputSanitizer.sanitize(""));
    }

    @Test
    void sanitize_shouldPassThroughCleanInput() {
        assertEquals("João da Silva", InputSanitizer.sanitize("João da Silva"));
        assertEquals("test@email.com", InputSanitizer.sanitize("test@email.com"));
        assertEquals("+49 170 1234567", InputSanitizer.sanitize("+49 170 1234567"));
    }
}
