package com.service.repos;

import com.service.entities.Product;
import com.service.entities.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstockRepo extends JpaRepository<Stock,Long> {
    public Stock findStockByProduct(Product product);

}
