package com.service.repos;

import com.service.entities.Product;
import com.service.entities.Quantity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuantityRepo extends JpaRepository<Quantity,Long> {
    List<Quantity> findAllByProduct(Product product);

}
