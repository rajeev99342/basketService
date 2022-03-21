package com.service.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "PRODUCT")
public class Product{
    @Id
    @Column(name = "PRODUCT_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PRODUCT_NAME")
    private String prodName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CAT_ID",referencedColumnName = "CAT_ID")
    private Category category;

    @Column(name = "PRODUCT_PRICE")
    private Double prodPrice;

    @Column(name = "PRODUCT_DESC")
    private String prodDesc;

    @Column(name = "BRAND")
    private  String  prodBrand;

    @Column(name = "WEIGHT")
    private Double prodWeight;


}
