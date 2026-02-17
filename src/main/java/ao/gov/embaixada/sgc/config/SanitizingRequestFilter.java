package ao.gov.embaixada.sgc.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Order(2)
public class SanitizingRequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(new SanitizedRequestWrapper(request), response);
    }

    private static class SanitizedRequestWrapper extends HttpServletRequestWrapper {

        SanitizedRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            return InputSanitizer.sanitize(super.getParameter(name));
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) {
                return null;
            }
            return Arrays.stream(values)
                    .map(InputSanitizer::sanitize)
                    .toArray(String[]::new);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return super.getParameterMap().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> Arrays.stream(e.getValue())
                                    .map(InputSanitizer::sanitize)
                                    .toArray(String[]::new)
                    ));
        }
    }
}
