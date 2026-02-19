package ao.gov.embaixada.sgc.config;

import ao.gov.embaixada.commons.security.filter.SanitizingRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SanitizingRequestFilterTest {

    /**
     * Test subclass that exposes the protected {@code doFilterInternal} method
     * of {@link SanitizingRequestFilter} so it can be called from a different package.
     */
    static class TestableSanitizingRequestFilter extends SanitizingRequestFilter {

        @Override
        public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
            super.doFilterInternal(request, response, filterChain);
        }
    }

    private final TestableSanitizingRequestFilter filter = new TestableSanitizingRequestFilter();

    @Test
    void shouldSanitizeQueryParameters() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/cidadaos");
        request.setParameter("nome", "<script>alert('xss')</script>");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        HttpServletRequest wrappedRequest = (HttpServletRequest) chain.getRequest();
        assertNotNull(wrappedRequest);
        assertEquals("alert('xss')", wrappedRequest.getParameter("nome"));
    }

    @Test
    void shouldSanitizeParameterValues() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/search");
        request.addParameter("tags", "<b>bold</b>");
        request.addParameter("tags", "<i>italic</i>");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        HttpServletRequest wrappedRequest = (HttpServletRequest) chain.getRequest();
        assertNotNull(wrappedRequest);
        String[] values = wrappedRequest.getParameterValues("tags");
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("bold", values[0]);
        assertEquals("italic", values[1]);
    }

    @Test
    void shouldPassCleanParametersUnchanged() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/cidadaos");
        request.setParameter("nome", "João da Silva");
        request.setParameter("email", "joao@email.com");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        HttpServletRequest wrappedRequest = (HttpServletRequest) chain.getRequest();
        assertNotNull(wrappedRequest);
        assertEquals("João da Silva", wrappedRequest.getParameter("nome"));
        assertEquals("joao@email.com", wrappedRequest.getParameter("email"));
    }
}
