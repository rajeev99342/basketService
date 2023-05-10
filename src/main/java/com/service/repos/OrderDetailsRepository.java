package com.service.repos;

import com.service.constants.enums.OrderStatus;
import com.service.entities.Order;
import com.service.entities.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailsRepository extends JpaRepository<OrderDetails,Long> {
    List<OrderDetails> findProductDeliveryByOrder(Order order);
    List<OrderDetails> findProductDeliveryByOrderAndOrderStatus(Order order, OrderStatus status);

}
