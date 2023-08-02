package com.service.model.seller;

import com.service.constants.enums.ShopType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerDetailModel {
    private Long id;
    private String phone;
    private String shopName;
    private String shopAddress;
    private String shopOwnerName;
    private String gst;
    private String regNo;
    private ShopType shopType;
}
