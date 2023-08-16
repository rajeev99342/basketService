package com.service.service.seller;

import com.service.constants.enums.UserRole;
import com.service.entities.SellerDetails;
import com.service.entities.User;
import com.service.model.GlobalResponse;
import com.service.model.seller.SellerDetailModel;
import com.service.repos.SellerDetailsRepo;
import com.service.repos.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SellerServiceHandlerImpl implements SellerServiceHandler {

    @Autowired
    private SellerDetailsRepo sellerDetailsRepo;
    @Autowired
    private UserRepo userRepo;

    @Override
    public GlobalResponse getSellerList(int page, int size) {
        try {
            Pageable pageable =
                    PageRequest.of(page, size);
            List<String> roles = new ArrayList<>();
            roles.add(UserRole.DELIVERY.name());
            Page<SellerDetails> userList = sellerDetailsRepo.findAll(pageable);
            if (userList != null) {
                return GlobalResponse.getSuccess(userList.getContent());
            }
        } catch (Exception ex) {
            log.error("----------->> Failed to fetch seller list due to : {}", ex.getLocalizedMessage());
        }

        return GlobalResponse.getFailure(null);

    }

    @Override
    public GlobalResponse saveSellerDetails(SellerDetailModel sellerDetailModel) {
        try {
            User seller = userRepo.findUserByPhone(sellerDetailModel.getPhone());
            if (seller == null) {
                return GlobalResponse.getFailure("Seller not found, Should be customer first");
            }
            SellerDetails sellerDetails = sellerDetailsRepo.findByShopName(sellerDetailModel.getShopName());
            if (sellerDetails != null & sellerDetailModel.getId() == null) {
                String message = String.format("Shop with %s already exist", sellerDetails.getShopName());
                return GlobalResponse.getSuccess(message);
            }
            List<UserRole> roles = seller.getRoles();
            roles.add(UserRole.SELLER);
            sellerDetails = new SellerDetails();
            sellerDetails.setGst(sellerDetailModel.getGst());
            sellerDetails.setPhone(sellerDetailModel.getPhone());
            sellerDetails.setShopAddress(sellerDetailModel.getShopAddress());
            sellerDetails.setRegNo(sellerDetailModel.getRegNo());
            sellerDetails.setShopName(sellerDetailModel.getShopName());
            sellerDetails.setShopType(sellerDetailModel.getShopType());
            sellerDetails.setShopOwnerName(sellerDetailModel.getShopOwnerName());
            sellerDetailsRepo.save(sellerDetails);
            userRepo.save(seller);
            return GlobalResponse.getSuccess(sellerDetails.getId());
        } catch (Exception ex) {
            log.error("----------->> Failed to save seller details due to  : {}", ex.getLocalizedMessage());
        }
        return GlobalResponse.getFailure("Failed to save seller details");
    }
}
