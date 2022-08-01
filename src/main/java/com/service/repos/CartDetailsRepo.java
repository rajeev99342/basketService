package com.service.repos;

import com.service.entities.Cart;
import com.service.entities.CartDetails;
import com.service.entities.Product;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartDetailsRepo extends JpaRepository<CartDetails,Long> {
    List<CartDetails> findCartDetailsByCart(Cart cart);
    CartDetails findCartDetailsByProduct(Product product);

    CartDetails findCartDetailsByCartAndProduct(Cart cart,Product product);

   Boolean deleteCartDetailsByCartAndProduct(Cart cart , Product product);

    @Override
    void delete(CartDetails entity);

    @Query(
            value = "SELECT count(*) FROM cart_details cart WHERE cart.cart_id=?1",
            nativeQuery = true)
    Integer getCountOfProductByCart(Long cartId);
}
