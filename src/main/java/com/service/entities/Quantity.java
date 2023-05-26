package com.service.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "PRODUCT_QUANTITY")
public class Quantity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID",referencedColumnName = "ID")
    private Product product;

    @Column(name = "UNIT")
    private String unit;

    @Column(name = "QUANTITY")
    private Double quantity;

    @Column(name = "PRICE")
    private Double price;


    @Column(name = "IN_STOCK")
    private Double inStock ;

    @Column(name = "QUANT_IN_PACKET")
    private Double quantityInPacket;

    @Column(name = "QUANT_IN_PACKET_UNIT")
    private String quantityInPacketUnit;


}
