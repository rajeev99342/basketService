package com.service.utilites;

import com.service.constants.enums.PaymentModeEnum;
import com.service.constants.values.PaymentModeValue;
import org.springframework.stereotype.Component;

@Component
public class Payment {
    public PaymentModeEnum getPaymentMode(String mode){

        switch (mode){
            case PaymentModeValue.CASE_ON_DELIVERY:
                return PaymentModeEnum.CASE_ON_DELIVERY;
            case PaymentModeValue.CARD:
                return PaymentModeEnum.CARD;
            default:
                return PaymentModeEnum.CASE_ON_DELIVERY;
        }
    }
}
