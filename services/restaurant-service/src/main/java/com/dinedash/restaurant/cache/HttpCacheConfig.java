package com.dinedash.restaurant.cache;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import java.io.IOException;

/**
 * Browser-side caching for the public catalog. Responses get an ETag, so a repeat
 * request with If-None-Match is answered with 304 and no body, plus a short max-age
 * so listings stay fresh.
 */
@Configuration
public class HttpCacheConfig {

    private static final String CATALOG = "/api/restaurants/*";

    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> catalogEtagFilter() {
        FilterRegistrationBean<ShallowEtagHeaderFilter> bean = new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
        bean.addUrlPatterns("/api/restaurants", CATALOG, CATALOG + "/menu");
        bean.setName("catalogEtagFilter");
        return bean;
    }

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> catalogCacheControlFilter() {
        OncePerRequestFilter filter = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {
                if ("GET".equals(request.getMethod())) {
                    // public: may be cached by the browser and CDN; revalidate after 30s
                    response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=30, stale-while-revalidate=60");
                }
                chain.doFilter(request, response);
            }
        };
        FilterRegistrationBean<OncePerRequestFilter> bean = new FilterRegistrationBean<>(filter);
        bean.addUrlPatterns("/api/restaurants", CATALOG, CATALOG + "/menu");
        bean.setName("catalogCacheControlFilter");
        return bean;
    }
}
