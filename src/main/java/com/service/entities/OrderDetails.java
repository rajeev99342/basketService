package com.service.entities;

import com.service.constants.enums.OrderStatus;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "ORDER_DETAILS")
public class OrderDetails {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID",referencedColumnName = "ID")
    private Product product;


    @Column(name = "QUANTITY")
    private Integer quantity;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID",referencedColumnName = "ORDER_ID")
    private Order order;

    @Column(name = "ITEM_PRICE")
    private Double itemPrice;

    @Column(name = "DISCOUNT_ON_THIS_ITEM")
    private Double itemDiscount;

    @Enumerated(EnumType.STRING)
    @Column(name = "ORDER_STATUS")
    private OrderStatus orderStatus;

    @Column(name = "TXN_ID")
    private OrderStatus transactionId;


}
