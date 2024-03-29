package com.service.entities;

import com.service.constants.enums.OrderStatus;
import com.service.constants.enums.PaymentModeEnum;
import com.service.constants.enums.YESNO;
import lombok.Data;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "`ORDER`")
public class Order {
    @Id
    @Column(name = "ORDER_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.REMOVE)
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

    @Column(name = "MODIFIED_DATE")
    private Date modifiedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAID")
    private YESNO yesNo;

    @Column(name = "TXN_ID")
    private String transactionId;


    @Column(name = "REFUND_TXN_ID")
    private String refundTxnId;


    @Column(name = "ORDER_DELIVERED_DATE")
    private Date orderDeliveredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DELIVERY_AGENT_ID",referencedColumnName = "USER_ID")
    private User deliveryAgent;

    @Column(name = "LATITUDE")
    private String latitude;

    @Column(name = "LONGITUDE")
    private String longitude;

    @Column(name = "ADDRESS")
    private String addressLine;

    @Column(name = "LANDMARK")
    private String landmark;

    @OneToMany(mappedBy = "order", cascade = CascadeType.REMOVE,orphanRemoval = true)
    private List<OrderDetails> children = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.REMOVE,orphanRemoval = true)
    private List<OrderSeller> orderSellers = new ArrayList<>();

}
