package com.service.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
@Data
@Entity
@Table(name = "CART")
public class Cart {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @JoinColumn(name = "USER_ID",referencedColumnName = "USER_ID")
    @OneToOne
    User user;

    @Column(name = "SELECTED_SIZE")
    Integer selectedSize;

    @Column(name = "SELECTED_WEIGHT")
    Double selectedWeight;

    @Column(name = "SELECTED_COUNT")
    Integer selectedCount;

    @JoinColumn(name = "PRODUCT_ID")
    @ManyToOne
    Product product;

    @Column(name = "CREATED_AT")
    Date createdAt;

    @Column(name = "UPDATED_AT")
    Date updatedAt;


}
