package com.service.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "CART_DETAILS")
public class CartDetails {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @JoinColumn(name = "CART_ID",referencedColumnName = "CART_ID")
    @ManyToOne
    Cart cart;

    @Column(name = "SELECTED_SIZE")
    Integer selectedSize;

    @Column(name = "SELECTED_WEIGHT")
    Double selectedWeight;

    @Column(name = "SELECTED_COUNT")
    Integer selectedCount;

    @JoinColumn(name = "PRODUCT_ID",referencedColumnName = "ID")
    @OneToOne
    Product product;


    @JoinColumn(name = "QUANTITY_ID",referencedColumnName = "ID")
    @OneToOne
    Quantity quantity;

    @Column(name = "CREATED_AT")
    Date createdAt;

    @Column(name = "UPDATED_AT")
    Date updatedAt;


}
