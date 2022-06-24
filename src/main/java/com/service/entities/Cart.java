package com.service.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "CART")
public class Cart {
    @Id
    @Column(name = "CART_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @JoinColumn(name = "USER_ID")
    @OneToOne
    User user;

    @Column(name = "CREATED_AT")
    Date createdAt;

}
