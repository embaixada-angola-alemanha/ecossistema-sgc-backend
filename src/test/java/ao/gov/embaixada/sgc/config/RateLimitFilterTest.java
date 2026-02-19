package ao.gov.embaixada.sgc.config;

import ao.gov.embaixada.commons.security.filter.RateLimitFilter;
import ao.gov.embaixada.commons.security.filter.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitFilterTest {

    /**
     * Test subclass that exposes the protected methods of {@link RateLimitFilter}
     * so they can be called from a different package.
     */
    static class TestableRateLimitFilter extends RateLimitFilter {

        TestableRateLimitFilter(RateLimitProperties properties) {
            super(properties);
        }

        @Override
        public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
            super.doFilterInternal(request, response, filterChain);
        }

        @Override
        public boolean shouldNotFilter(HttpServletRequest request) {
            return super.shouldNotFilter(request);
        }
    }

    private TestableRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setRequestsPerMinute(5);
        filter = new TestableRateLimitFilter(properties);
    }

    @Test
    void shouldAllowRequestsWithinLimit() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/visas");
        request.setRemoteAddr("192.168.1.1");

        for (int i = 0; i < 5; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();
            filter.doFilterInternal(request, response, chain);
            assertEquals(200, response.getStatus(), "Request " + (i + 1) + " should pass");
        }
    }

    @Test
    void shouldReturn429WhenLimitExceeded() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/visas");
        request.setRemoteAddr("10.0.0.1");

        // Exhaust the limit
        for (int i = 0; i < 5; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilterInternal(request, response, new MockFilterChain());
        }

        // Next request should be rate limited
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, new MockFilterChain());
        assertEquals(429, response.getStatus());
        assertTrue(response.getContentAsString().contains("Rate limit exceeded"));
    }

    @Test
    void shouldTrackDifferentIpsSeparately() throws Exception {
        MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/api/v1/visas");
        request1.setRemoteAddr("10.0.0.1");

        MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/api/v1/visas");
        request2.setRemoteAddr("10.0.0.2");

        // Exhaust limit for IP 1
        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(request1, new MockHttpServletResponse(), new MockFilterChain());
        }

        // IP 2 should still be allowed
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request2, response, new MockFilterChain());
        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldUseXForwardedForHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/visas");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.50, 70.41.3.18");

        // Exhaust limit for forwarded IP
        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());
        }

        // Same forwarded IP should be limited
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, new MockFilterChain());
        assertEquals(429, response.getStatus());

        // Different forwarded IP should pass
        MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/api/v1/visas");
        request2.setRemoteAddr("127.0.0.1");
        request2.addHeader("X-Forwarded-For", "198.51.100.10");
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        filter.doFilterInternal(request2, response2, new MockFilterChain());
        assertEquals(200, response2.getStatus());
    }

    @Test
    void shouldNotFilterActuatorPaths() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilterSwaggerPaths() throws Exception {
        MockHttpServletRequest swaggerUi = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        MockHttpServletRequest apiDocs = new MockHttpServletRequest("GET", "/v3/api-docs");
        assertTrue(filter.shouldNotFilter(swaggerUi));
        assertTrue(filter.shouldNotFilter(apiDocs));
    }
}
