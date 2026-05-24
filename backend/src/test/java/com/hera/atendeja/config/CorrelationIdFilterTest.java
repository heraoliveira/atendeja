package com.hera.atendeja.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void shouldReuseIncomingCorrelationIdAndExposeItInResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> correlationIdInsideChain = new AtomicReference<>();
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "audit-123");

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                correlationIdInsideChain.set(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY))
        );

        assertThat(correlationIdInsideChain).hasValue("audit-123");
        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo("audit-123");
        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
    }

    @Test
    void shouldGenerateCorrelationIdWhenHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> correlationIdInsideChain = new AtomicReference<>();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                correlationIdInsideChain.set(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY))
        );

        assertThat(correlationIdInsideChain.get()).isNotBlank();
        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER))
                .isEqualTo(correlationIdInsideChain.get());
        assertThat(UUID.fromString(correlationIdInsideChain.get())).isNotNull();
        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
    }
}
