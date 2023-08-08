package com.service.repos;

import com.service.constants.enums.OrderStatus;
import com.service.entities.Order;
import com.service.entities.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderDetailsRepository extends JpaRepository<OrderDetails,Long> {
    List<OrderDetails> findProductDeliveryByOrder(Order order);
    List<OrderDetails> findProductDeliveryByOrder_Id(Long id);
    List<OrderDetails> findProductDeliveryByOrderAndOrderStatus(Order order, OrderStatus status);

    @Modifying
    @Query(value = "select * from order_details as od where od.order_id in (:orderIds)", nativeQuery = true)
    List<OrderDetails> findOrderDetailsList(@Param("orderIds") List<Long> orderIds);

    @Modifying
    @Query(value = "DELETE from order_details as od where od.order_id in (:orderIds)", nativeQuery = true)
    void deleteByOrderId(@Param("orderIds") List<Long> orderIds);
}
