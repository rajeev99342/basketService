package com.service.model;
import lombok.Data;

import java.util.Date;

@Data
public class CartModel {
    private UserModel userModel;
    private Date createdAt;
    private Date modifiedAt;
}
