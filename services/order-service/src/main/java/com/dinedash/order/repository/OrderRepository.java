package com.dinedash.order.repository;

import com.dinedash.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerIdOrderByPlacedAtDesc(Long customerId, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Optional<Order> findWithItemsById(Long id);
}
