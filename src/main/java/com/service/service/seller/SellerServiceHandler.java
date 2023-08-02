package com.service.service.seller;

import com.service.model.GlobalResponse;
import com.service.model.seller.SellerDetailModel;
import org.springframework.data.domain.Pageable;

public interface SellerServiceHandler {
    GlobalResponse getSellerList(int page,int size);

    GlobalResponse saveSellerDetails(SellerDetailModel sellerDetailModel);
}
