package com.service.entities;

import com.service.constants.enums.OrderStatus;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "DELIVERY_TIME")

public class DeliveryTime {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hrs")
    private Integer hr;

}
