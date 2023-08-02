package com.service.utilites;

import com.service.entities.Address;
import com.service.entities.SellerDetails;
import com.service.model.AddressModel;
import com.service.model.seller.SellerDetailModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
public class SellerModelConvertor {

    public Function<List<SellerDetails>, List<SellerDetailModel>> CONVERT_INTO_MODEL = sellerList -> {
        List<SellerDetailModel> modelList = new ArrayList<>();
        if(sellerList != null && sellerList.size() > 0){
            for(SellerDetails seller : sellerList){
                SellerDetailModel model = new SellerDetailModel();
                model.setId(seller.getId());
                model.setPhone(seller.getPhone());
                model.setRegNo(seller.getRegNo());
                model.setGst(seller.getGst());
                model.setShopAddress(seller.getShopAddress());
                model.setShopName(seller.getShopName());
                model.setShopType(seller.getShopType());
                model.setShopOwnerName(seller.getShopOwnerName());
                modelList.add(model);
            }

        }
        return modelList;

    };


}
