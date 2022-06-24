package com.service.repos;

import com.service.entities.Order;
import com.service.entities.ProductDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductDeliveryRepo extends JpaRepository<ProductDelivery,Long> {
    List<ProductDelivery> findProductDeliveryByOrder(Order order);
}
