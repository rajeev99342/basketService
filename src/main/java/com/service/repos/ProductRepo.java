package com.service.repos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.service.entities.Category;
import com.service.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ProductRepo extends PagingAndSortingRepository<Product,Long> {
    Product findProductByIdAndIsValid(Long id,Boolean isValid);
    List<Product> findProductByIsValid(Boolean isValid,Pageable pageable);
    List<Product> findProductByCategoryAndIsValid(Category category,Boolean isValid,Pageable pageable);

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    List<Product> findProductByCategory_IdAndIsValid(Long catId,Boolean isValid,Pageable pageable);

    @Query(value = "SELECT * FROM PRODUCT WHERE MATCH(NAME, DESCRIPTION, BRAND) "+ "AGAINST (?1)", nativeQuery = true)
    public Page<Product> search(String keyword, Pageable pageable);

    @Query(value = "SELECT * FROM PRODUCT", nativeQuery = true)
    List<Product> findAllProduct(Pageable pageable);
    List<Product> findByNameContains(String name, Pageable pageable);
}
