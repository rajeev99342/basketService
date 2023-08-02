package com.service.entities;

import com.service.constants.enums.ShopType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "`SELLER_DETAILS`")
public class SellerDetails {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "PHONE")
    private String phone;

    @Column(name = "SHOP_NAME")
    private String shopName;

    @Column(name = "SHOP_ADDRESS")
    private String shopAddress;


    @Column(name = "OWNER_NAME")
    private String shopOwnerName;

    @Column(name = "GST")
    private String gst;


    @Column(name = "REG_NO")
    private String regNo;


    @Enumerated(EnumType.STRING)
    @Column(name = "SHOP_TYPE")
    private ShopType shopType;

    @Column(name = "lat")
    private String lat;

    @Column(name = "lon")
    private String lon;

}
