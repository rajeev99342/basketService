package com.service.repos;

import com.service.entities.Cart;
import com.service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepo extends JpaRepository<Cart,Long> {
    Cart findCartByUser(User user);
}
