package com.service.repos;

import com.service.constants.enums.OrderStatus;
import com.service.entities.Order;
import com.service.entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface OrderRepo extends JpaRepository<Order,Long> {
//        @Query(value = "SELECT o FROM Order o WHERE o.orderStatus IN :orderStatus AND o.user = :user",nativeQuery = true)
//        List<Order> findOrderByUserAndStatus(@Param("user") User user,@Param("orderStatus") List<String> orderStatus);
        List<Order> findOrderByUser(User user, Pageable pageable);
        List<Order> findOrderByOrderDate(Date orderDate);
        List<Order> findOrderByOrderStatus(OrderStatus status);



}
