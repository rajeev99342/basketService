package com.service.repos;

import com.service.entities.SellerDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SellerDetailsRepo extends JpaRepository<SellerDetails,Long> {
    SellerDetails findByShopName(String shopName);

    @Query(value = "select * from seller_details as sd inner join order_seller as shop on shop.seller_phone = sd.phone and shop.order_id = :id", nativeQuery = true)
    List<SellerDetails> sellerDetailsByOrderId(@Param("id") Long id);


    @Query(value = "select * from seller_details as sd inner join order_seller as shop on shop.seller_phone = sd.phone and shop.order_id = :id and shop.shop_status = :status", nativeQuery = true)
    List<SellerDetails> sellerDetailsByOrderIdAndStatus(@Param("id") Long id,@Param("status") String status);


}
