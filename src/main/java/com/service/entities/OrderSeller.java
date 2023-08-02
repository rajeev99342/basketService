package com.service.entities;

import com.service.constants.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "`ORDER_SELLER`")
public class OrderSeller {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID",referencedColumnName = "ORDER_ID")
    private Order order;

    @Column(name = "SELLER_PHONE")
    private String sellerPhone;


    @Column(name = "BUYER_PHONE")
    private String buyerPhone;


    @Enumerated(EnumType.STRING)
    @Column(name = "SHOP_STATUS")
    private OrderStatus orderStatus;
}
