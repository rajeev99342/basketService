package com.service.repos;

import com.service.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepo extends JpaRepository<Product,Long> {
    Product findProductByIdAndIsValid(Long id,Boolean isValid);
    List<Product> findProductByIsValid(Boolean isValid);
}
