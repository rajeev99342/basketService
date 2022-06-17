package com.service.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "CART_PRODUCT_MAPPING")
public class CartProductMapping {
    @Id
    @Column(name = "CART_PRO_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @JoinColumn(name = "PRODUCT_ID")
    @OneToOne
    Product product;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "CART_ID")
    Cart cart;

}
