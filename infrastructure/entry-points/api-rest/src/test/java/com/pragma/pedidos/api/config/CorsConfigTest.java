package com.pragma.pedidos.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.filter.CorsFilter;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CorsConfigTest {
    @Test
    void corsFilter_ShouldReturnFilterRegistrationBean() {
        CorsConfig corsConfig = new CorsConfig();
        FilterRegistrationBean<CorsFilter> bean = corsConfig.corsFilter(List.of("http://localhost:4200"));
        assertNotNull(bean);
    }
}