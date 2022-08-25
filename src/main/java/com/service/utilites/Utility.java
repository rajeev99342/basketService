package com.service.utilites;

import com.service.entities.Product;
import com.service.entities.Quantity;
import com.service.repos.QuantityRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Utility {

    @Autowired
    QuantityRepo quantityRepo;
    public void deletePreviousQuantityList(Product product){
        List<Quantity> quantities = quantityRepo.findAllByProduct(product);
        for(Quantity quantity : quantities){
                quantityRepo.delete(quantity);
        }
    }
}
