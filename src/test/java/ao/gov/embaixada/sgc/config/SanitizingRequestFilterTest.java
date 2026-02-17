package ao.gov.embaixada.sgc.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SanitizingRequestFilterTest {

    private final SanitizingRequestFilter filter = new SanitizingRequestFilter();

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
