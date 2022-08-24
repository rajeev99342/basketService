package com.service.entities;

import com.service.constants.enums.OrderStatus;
import com.service.constants.enums.PaymentModeEnum;
import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "`ORDER`")
public class Order {
    @Id
    @Column(name = "ORDER_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID",referencedColumnName = "USER_ID")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private OrderStatus orderStatus;

    @Column(name = "TOTAL_COST")
    private Double totalCost;


    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_MODE")
    private PaymentModeEnum paymentMode;

    @Column(name = "ORDER_DATE")
    private Date orderDate;

    @Column(name = "ORDER_EXPECTED_DELIVERY_DATE")
    private Date expectedDeliveryDate;

    @Column(name = "ORDER_DELIVERED_DATE")
    private Date orderDeliveredAt;
}
