package com.dinedash.payment.web;

import com.dinedash.common.error.ApiError;
import com.dinedash.common.error.BaseExceptionHandler;
import com.dinedash.payment.stripe.PaymentProviderException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(PaymentProviderException.class)
    public ResponseEntity<ApiError> handleProvider(PaymentProviderException ex, HttpServletRequest request) {
        log.error("Stripe error: {}", ex.getMessage());
        return build(HttpStatus.BAD_GATEWAY, "Payment provider error, please try again", request, List.of());
    }
}
