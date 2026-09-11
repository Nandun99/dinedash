package com.dinedash.order.client;

import com.dinedash.common.error.BadRequestException;
import com.dinedash.common.error.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class RestaurantClient {

    private final RestClient restClient;

    public RestaurantClient(RestClient.Builder builder, @Value("${dinedash.services.restaurant-url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = builder.baseUrl(baseUrl).requestFactory(factory).build();
    }

    public Quote quote(String restaurantId, List<QuoteLine> lines) {
        try {
            return restClient.post()
                    .uri("/internal/restaurants/{id}/quote", restaurantId)
                    .body(new QuoteRequest(lines))
                    .retrieve()
                    .body(Quote.class);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException("Restaurant not found");
            }
            throw new BadRequestException("Some items in your cart are no longer available");
        } catch (ResourceAccessException ex) {
            log.error("Restaurant service unreachable", ex);
            throw new IllegalStateException("Restaurant service unavailable", ex);
        }
    }

    public record QuoteLine(String menuItemId, int quantity) {
    }

    record QuoteRequest(List<QuoteLine> items) {
    }

    public record Quote(String restaurantId, String restaurantName, double latitude, double longitude,
                        boolean acceptingOrders, BigDecimal deliveryFee, BigDecimal minimumOrder,
                        List<PricedLine> items) {
    }

    public record PricedLine(String menuItemId, String name, BigDecimal unitPrice, int quantity, boolean available) {
    }
}
