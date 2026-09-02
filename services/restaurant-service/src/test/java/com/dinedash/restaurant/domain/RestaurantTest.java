package com.dinedash.restaurant.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantTest {

    @Test
    void openDuringNormalHours() {
        Restaurant r = withHours("10:00", "22:00");

        assertThat(r.isOpenAt(at("12:30"))).isTrue();
        assertThat(r.isOpenAt(at("09:59"))).isFalse();
        assertThat(r.isOpenAt(at("22:00"))).isFalse();
    }

    @Test
    void handlesHoursPastMidnight() {
        Restaurant r = withHours("18:00", "02:00");

        assertThat(r.isOpenAt(at("23:30"))).isTrue();
        assertThat(r.isOpenAt(at("01:15"))).isTrue();
        assertThat(r.isOpenAt(at("03:00"))).isFalse();
    }

    @Test
    void pausedRestaurantCannotTakeOrders() {
        Restaurant r = withHours("10:00", "22:00");
        r.setAcceptingOrders(false);

        assertThat(r.canTakeOrders(at("12:00"))).isFalse();
    }

    private static Restaurant withHours(String open, String close) {
        Restaurant r = new Restaurant();
        r.setOpensAt(LocalTime.parse(open));
        r.setClosesAt(LocalTime.parse(close));
        return r;
    }

    private static ZonedDateTime at(String time) {
        return ZonedDateTime.of(2026, 10, 5, 0, 0, 0, 0, Restaurant.ZONE).with(LocalTime.parse(time));
    }
}
