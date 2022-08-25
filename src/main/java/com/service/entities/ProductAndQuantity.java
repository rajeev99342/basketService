package com.service.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "PRODUCT_AND_QUANTITY")
public class ProductAndQuantity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID",referencedColumnName = "ID")
    private Product product;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QUANTITY_ID",referencedColumnName = "ID")
    private Quantity quantity;
}
