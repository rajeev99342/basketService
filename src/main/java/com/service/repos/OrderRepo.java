package com.service.repos;

import com.service.constants.enums.OrderStatus;
import com.service.entities.Order;
import com.service.entities.OrderSeller;
import com.service.entities.User;
import com.service.model.interfacemodel.ICountAmount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface OrderRepo extends JpaRepository<Order, Long> {
    //        @Query(value = "SELECT o FROM Order o WHERE o.orderStatus IN :orderStatus AND o.user = :user",nativeQuery = true)
//        List<Order> findOrderByUserAndStatus(@Param("user") User user,@Param("orderStatus") List<String> orderStatus);
    List<Order> findOrderByUser(User user, Pageable pageable);

    @Query(value = "select * from `order` as o inner join `user` as u on u.USER_ID=o.USER_ID where u.USER_ID= :user_id", nativeQuery = true)
    List<Order> findOrderByUser(@Param("user_id") Long user_id, Pageable pageable);


    @Query(value = "select * from `order` as o inner join `user` as u on u.USER_ID=o.USER_ID where u.USER_ID= :user_id and o.ORDER_DATE >= :order_date", nativeQuery = true)
    List<Order> findOrderByDate(@Param("user_id") Long user_id, @Param("order_date") Date order_date, Pageable pageable);

//        @Query(value = "select * from `order` as o inner join `user` as u on u.USER_ID=o.user_id",nativeQuery = true)
//        List<Order> findOrderByDate(@Param("user_id") Long user_id);
//

    List<Order> findOrderByOrderStatus(OrderStatus status, Pageable pageable);

    List<Order> findOrderByOrderStatusAndUser(OrderStatus status, User user);


    @Query(value = "select count(*) as count, sum(o.TOTAL_COST) as amount from `order` as o inner join `user` as u on u.USER_ID=o.USER_ID where u.PHONE= :phone and o.STATUS = :status", nativeQuery = true)
    ICountAmount findCountOfOrderByStatusAndUser(@Param("phone") String phone, @Param("status") String status);

    List<Order> findOrderByOrderStatusIn(List<OrderStatus> statuses, Pageable pageable);


    @Query(value = "SELECT * FROM `order` AS o WHERE EXISTS ( SELECT 1 FROM `order_seller` AS shop WHERE o.order_id = shop.order_id AND shop.shop_status = :status AND shop.seller_phone= :seller)", nativeQuery = true)
    List<Order> findOrderFromShops(@Param("seller") String seller, @Param("status") String status, Pageable pageable);

    @Query(value = "SELECT * FROM `order` AS o  where o.status = :status", nativeQuery = true)
    List<Order> findOrderFromShopsForAdmin(@Param("status") String status, Pageable pageable);


    @Query(value = "SELECT * FROM `order` AS o WHERE o.delivery_agent_id = :id and o.status = :status", nativeQuery = true)
    List<Order> findAllOrderByDelivery(@Param("status") String status, @Param("id")  Long id,Pageable pageable);


    @Query(value = "SELECT count(*) FROM `order` AS o WHERE o.user_id = :userId", nativeQuery = true)
    int getCountByUserID(@Param("userId")  Long userId);

    @Query(value = "SELECT * FROM `order` AS o WHERE o.user_id = :userId", nativeQuery = true)
    Page<Order> getCountByUserID(@Param("userId")  Long userId, Pageable pageable);

    @Modifying
    @Query(value = "DELETE FROM `order` as o WHERE o.user_id = :userId", nativeQuery = true)
    void deleteAllByUser(@Param("userId")  Long userId);
}
