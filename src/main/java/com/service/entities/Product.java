package com.service.entities;

import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "product",indexes = {
        @Index(name = "ID", columnList = "ID ASC"),
        @Index(name = "CAT_ID", columnList = "CAT_ID ASC")

})
public class Product{
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME",columnDefinition = "TEXT")
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CAT_ID",referencedColumnName = "CAT_ID")
    private Category category;


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

    @Column(name = "SELLER_ID")
    private Long sellerId;


    @Column(name = "CREATED_AT")
    private Date createdAt;

    @Column(name = "UPDATED_AT")
    private Date updatedAt;


    @ManyToMany
    private List<CartDetails> cartDetails;


}
