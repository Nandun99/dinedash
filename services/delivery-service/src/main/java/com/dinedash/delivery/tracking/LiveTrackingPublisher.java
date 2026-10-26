package com.dinedash.delivery.tracking;

import com.dinedash.delivery.domain.Delivery;

/**
 * Pushes live delivery state to the browser. The customer's tracking page subscribes to
 * {@code /deliveries/{orderId}} in Firebase Realtime Database.
 */
public interface LiveTrackingPublisher {

    void publishLocation(Long orderId, double latitude, double longitude);

    void publishStatus(Delivery delivery);
}
