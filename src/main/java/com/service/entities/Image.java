package com.service.entities;

import com.service.constants.enums.ImgType;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "IMAGE")
public class Image {

    @Id
    @Column(name = "IMG_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PRODUCT_ID",referencedColumnName = "ID")
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CAT_ID",referencedColumnName = "CAT_ID")
    private Category category;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PR_ID",referencedColumnName = "PR_ID")
    private ProductReview productReview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID",referencedColumnName = "USER_ID")
    private User user;

    @OneToOne(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    @JoinColumn(name = "IMAGE_DETAILS_ID",referencedColumnName = "IMAGE_DETAILS_ID")
    private ImageDetails imageDetails;

    @Column(name = "IMG_TYPE")
    private ImgType imgType;



}
