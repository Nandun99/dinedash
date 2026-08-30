package com.dinedash.gateway.error;

import com.dinedash.common.error.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;

/**
 * Returns the same JSON error shape as the downstream services, including when a
 * service is down or the caller is not authenticated.
 */
@Slf4j
@Order(-2)
@Component
@RequiredArgsConstructor
public class GatewayErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }
        HttpStatusCode status;
        String message;
        if (ex instanceof ResponseStatusException rse) {
            status = rse.getStatusCode();
            message = rse.getReason() != null ? rse.getReason() : "Request failed";
        } else if (ex instanceof ConnectException || ex.getCause() instanceof ConnectException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Service temporarily unavailable";
        } else {
            log.error("Gateway error on {}", exchange.getRequest().getPath(), ex);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "Something went wrong";
        }
        HttpStatus resolved = HttpStatus.resolve(status.value());
        ApiError body = ApiError.of(status.value(), resolved != null ? resolved.getReasonPhrase() : "Error",
                message, exchange.getRequest().getPath().value());

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            DataBuffer buffer = response.bufferFactory().wrap(objectMapper.writeValueAsBytes(body));
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
