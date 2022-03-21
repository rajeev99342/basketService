package com.service.entities;

import lombok.Data;

import javax.persistence.*;
@Data
@Entity
@Table(name = "STOCK")
public class Stock {

    @Id
    @Column(name = "STOCK_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID",referencedColumnName = "PRODUCT_ID")
    private Product product;

    @Column(name = "IN_STOCK")
    private Long inStock;


}
