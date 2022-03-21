package com.service.entities;

import com.service.constants.OrderStatus;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "PRODUCT_DELIVERY")
public class ProductDelivery {
    @Id
    @Column(name = "PD_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID",referencedColumnName = "PRODUCT_ID")
    private Product product;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID",referencedColumnName = "ORDER_ID")
    private Order order;

    @Column(name = "DELIVERY_DATE")
    private Date deliveryDate;

    @Column(name = "SELLER_ID")
    private Long sellerId;

    @Column(name = "ORDER_STATUS")
    private OrderStatus orderStatus;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ADDRESS_ID",referencedColumnName = "ADDRESS_ID")
    private Address address;
}
