package com.service.repos;

import com.service.entities.Order;
import com.service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepo extends JpaRepository<Order,Long> {
        List<Order> findOrderByUser(User user);
}
