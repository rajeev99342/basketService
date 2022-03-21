package com.service.entities;

import com.service.constants.OrderStatus;
import com.service.constants.PAYMENTMODE;
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

    @Column(name = "STATUS")
    private OrderStatus orderStatus;

    @Column(name = "TOTAL_COST")
    private Double totalCost;

    @Column(name = "PAYMENT_MODE")
    private PAYMENTMODE paymentMode;

    @Column(name = "ORDER_DATE")
    private Date orderDate;


}
