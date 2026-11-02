package com.dinedash.notification.service;

import java.util.Optional;

/** Customer-facing wording for each order status. */
public final class OrderMessages {

    private OrderMessages() {
    }

    public record Message(String title, String body) {
    }

    public static Optional<Message> forStatus(String status, Long orderId) {
        String ref = "Order #" + orderId;
        return Optional.ofNullable(switch (status) {
            case "PAID" -> new Message("Payment received", ref + " has been sent to the restaurant.");
            case "PAYMENT_FAILED" -> new Message("Payment failed", "We could not charge your card for " + ref + ".");
            case "ACCEPTED" -> new Message("Order accepted", "The restaurant is preparing " + ref + ".");
            case "READY_FOR_PICKUP" -> new Message("Food is ready", ref + " is ready and waiting for a driver.");
            case "PICKED_UP" -> new Message("On the way", "Your driver has picked up " + ref + ".");
            case "DELIVERED" -> new Message("Delivered", ref + " has been delivered. Enjoy your meal!");
            case "REJECTED" -> new Message("Order rejected", "The restaurant could not take " + ref + ". You will be refunded.");
            case "CANCELLED" -> new Message("Order cancelled", ref + " was cancelled.");
            default -> null;
        });
    }
}
