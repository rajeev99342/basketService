package com.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.service.constants.enums.OrderStatus;
import com.service.constants.enums.PaymentModeEnum;
import com.service.entities.User;
import com.service.model.seller.SellerDetailModel;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OrderRS {

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    private OrderStatus orderStatus;

    private Double totalCost;

    private PaymentModeEnum paymentMode;

    private Date orderDate;

    private String completeAddress;
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<ProductOrderDetails> deliveryProducts;
    private AddressModel addressModel;
    private Date expectedDeliveryDate ;
    private Long orderId;
    private String displayOrderStatus;
    private Boolean  isNew = true;
    private Date deliveredAt ;
    private Date lastModifiedDate;
    private String refundTxnId;
    private String txnId;
    private String latitude;
    private String longitude;
    private String addressLine;
    private Integer quantity;
    private String landmark;
    private List<SellerDetailModel> sellerDetailModels;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private transient User deliveryAgent;
}
