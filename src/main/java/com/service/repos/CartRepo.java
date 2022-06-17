package com.service.repos;

import com.service.entities.Cart;
import com.service.entities.Product;
import com.service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepo extends JpaRepository<Cart,Long> {
    Cart findCartByUserId(Long id);
    Cart findCartByProductAndUser(Product prod, User user);
}
