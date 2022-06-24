package com.service.repos;

import com.service.entities.Cart;
import com.service.entities.CartDetails;
import com.service.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartDetailsRepo extends JpaRepository<CartDetails,Long> {
    List<CartDetails> findCartDetailsByCart(Cart cart);
    CartDetails findCartDetailsByProduct(Product product);

    CartDetails findCartDetailsByCartAndProduct(Cart cart,Product product);
}
