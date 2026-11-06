package com.dinedash.order.repository;

import com.dinedash.order.domain.Order;
import com.dinedash.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerIdOrderByPlacedAtDesc(Long customerId, Pageable pageable);

    Page<Order> findAllByOrderByPlacedAtDesc(Pageable pageable);

    Page<Order> findByStatusOrderByPlacedAtDesc(OrderStatus status, Pageable pageable);

    @Query("select o.status as status, count(o) as count from Order o where o.placedAt >= :since group by o.status")
    List<StatusCount> countByStatusSince(Instant since);

    @Query("select coalesce(sum(o.total), 0) from Order o where o.placedAt >= :since and o.status = :status")
    BigDecimal sumTotalSince(Instant since, OrderStatus status);

    interface StatusCount {
        OrderStatus getStatus();

        long getCount();
    }

    @EntityGraph(attributePaths = "items")
    Optional<Order> findWithItemsById(Long id);
}
