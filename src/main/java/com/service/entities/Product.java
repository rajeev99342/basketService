package com.service.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "PRODUCT")
public class Product{
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CAT_ID",referencedColumnName = "CAT_ID")
    private Category category;

    @Column(name = "PRICE_PER_UNIT")
    private Double pricePerUnit;

    @Column(name = "UNIT")
    private String unit;

    @Column(name = "DESCRIPTION",columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "BRAND")
    private  String  prodBrand;

    @Column(name = "QUANTITY")
    private Double quantity;

    @Column(name = "SELLING_PRICE")
    private Double sellingPrice;

    @Column(name = "DISCOUNT")
    private Double discount;

    @Column(name = "IS_VALID")
    private Boolean isValid;

    @ManyToMany
    private List<CartDetails> cartDetails;

}
