package com.service.entities;

import com.service.constants.enums.Rating;
import lombok.Data;

import javax.persistence.*;
@Data
@Entity
@Table(name = "PRODUCT_REVIEW")
public class ProductReview {
    @Id
    @Column(name = "PR_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID",referencedColumnName = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID",referencedColumnName = "PRODUCT_ID")
    private Product product;

    @Column(name = "REVIEW")
    private String review;

    @Column(name = "RATING")
    private Rating rating;


}
