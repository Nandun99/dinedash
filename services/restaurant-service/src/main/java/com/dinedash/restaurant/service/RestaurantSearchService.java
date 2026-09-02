package com.dinedash.restaurant.service;

import com.dinedash.restaurant.domain.Restaurant;
import com.dinedash.restaurant.dto.PageResponse;
import com.dinedash.restaurant.dto.RestaurantCard;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Public restaurant discovery. With a location, results are sorted by distance using the
 * 2dsphere index; without one, they are sorted by name.
 */
@Service
@RequiredArgsConstructor
public class RestaurantSearchService {

    static final double DEFAULT_RADIUS_KM = 10;
    static final double MAX_RADIUS_KM = 30;
    static final int MAX_PAGE_SIZE = 50;

    private final MongoTemplate mongoTemplate;

    public PageResponse<RestaurantCard> search(SearchCriteria criteria) {
        int size = Math.min(Math.max(criteria.size(), 1), MAX_PAGE_SIZE);
        int page = Math.max(criteria.page(), 0);
        Criteria filter = buildFilter(criteria);

        // Fetch one extra row to know if there is a next page without a count query.
        if (criteria.latitude() != null && criteria.longitude() != null) {
            double radius = Math.min(criteria.radiusKm() != null ? criteria.radiusKm() : DEFAULT_RADIUS_KM, MAX_RADIUS_KM);
            NearQuery near = NearQuery.near(new Point(criteria.longitude(), criteria.latitude()), Metrics.KILOMETERS)
                    .spherical(true)
                    .maxDistance(new Distance(radius, Metrics.KILOMETERS))
                    .query(new Query(filter))
                    .skip((long) page * size)
                    .limit(size + 1);
            GeoResults<Restaurant> results = mongoTemplate.geoNear(near, Restaurant.class);
            List<RestaurantCard> cards = results.getContent().stream()
                    .map(r -> RestaurantCard.from(r.getContent(), round(r.getDistance().getValue())))
                    .toList();
            return toPage(cards, page, size);
        }

        Query query = new Query(filter)
                .with(Sort.by("name"))
                .skip((long) page * size)
                .limit(size + 1);
        List<RestaurantCard> cards = mongoTemplate.find(query, Restaurant.class).stream()
                .map(r -> RestaurantCard.from(r, null))
                .toList();
        return toPage(cards, page, size);
    }

    private static Criteria buildFilter(SearchCriteria criteria) {
        Criteria filter = new Criteria();
        if (StringUtils.hasText(criteria.query())) {
            filter.and("name").regex(Pattern.quote(criteria.query().trim()), "i");
        }
        if (StringUtils.hasText(criteria.cuisine())) {
            filter.and("cuisines").is(criteria.cuisine().trim().toLowerCase(Locale.ROOT));
        }
        return filter;
    }

    private static <T> PageResponse<T> toPage(List<T> rows, int page, int size) {
        boolean hasNext = rows.size() > size;
        return new PageResponse<>(hasNext ? rows.subList(0, size) : rows, page, size, hasNext);
    }

    private static double round(double km) {
        return Math.round(km * 10) / 10.0;
    }

    public record SearchCriteria(String query, String cuisine, Double latitude, Double longitude, Double radiusKm,
                                 int page, int size) {
    }
}
