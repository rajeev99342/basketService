package com.service.repos;

import com.service.entities.OrderSeller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderSellerRepo extends JpaRepository<OrderSeller,Long>{
    List<OrderSeller> findAllByOrder_Id(Long id);

    @Query(value = "SELECT * FROM `order_seller` AS os WHERE os.seller_phone= :seller AND os.order_id = :id", nativeQuery = true)
    OrderSeller findBySellerPhoneByOrderId(@Param("seller") String seller,@Param("id")  Long id);


    @Query(value = "SELECT * FROM `order_seller` AS os WHERE os.SHOP_STATUS= :status AND os.order_id = :id", nativeQuery = true)
    List<OrderSeller> findAllByStatusAndOrderId(@Param("status") String status,@Param("id")  Long id);


}
