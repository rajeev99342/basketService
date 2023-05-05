package com.service.utilites;

import com.service.entities.Address;
import com.service.model.AddressModel;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UserFunction {
    public Function<Address, AddressModel> CONVERT_INTO_MODEL = address -> {
        if(address != null){
            AddressModel model = new AddressModel();
            model.setArea(address.getArea());
            model.setCity(address.getCity());
            model.setLandmark(address.getLandmark());
            model.setMobile(address.getMobile());
            model.setAddressOne(address.getAddressOne());
            model.setPincode(address.getPincode());
            model.setId(address.getId());
            return model;
        }
        return null;

    };
}
