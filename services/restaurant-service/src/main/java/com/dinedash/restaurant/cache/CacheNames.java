package com.dinedash.restaurant.cache;

public final class CacheNames {

    /** Public restaurant details, keyed by restaurant id. */
    public static final String RESTAURANTS = "restaurants";
    /** Public menu (available items only), keyed by restaurant id. */
    public static final String MENUS = "menus";

    private CacheNames() {
    }
}
