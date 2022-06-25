package com.service.service;

import com.service.constants.enums.Role;
import com.service.entities.Address;
import com.service.entities.User;
import com.service.model.AddressModel;
import com.service.model.UserCredentials;
import com.service.repos.AddressRepo;
import com.service.repos.UserRepo;
import com.service.utilites.EncryptDecrypt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {


    @Autowired
    CartService cartService;
    @Autowired
    UserRepo userRepo;
    @Autowired
    AddressRepo addressRepo;

    @Autowired
    EncryptDecrypt encryptDecrypt;
    public void saveUserDetails(UserCredentials userCredentials) throws Exception {

        User user = userRepo.findUserByPhone(userCredentials.getMobile());
        if(!userCredentials.getPassword().equals(userCredentials.getConfirmedPassword())) {
            throw new Exception("Password doesn't matches");
        }else if(null != user){
                throw new Exception("User already registered by this phone number, try to login");
        }

        user = new User();
        user.setRoles(new ArrayList<Role>(Collections.singleton(Role.CUSTOMER)));
        user.setUserName(userCredentials.getName());
        user.setPhone(userCredentials.getMobile());
        String encryptedPassword = encryptDecrypt.encrypt(userCredentials.getPassword());
        user.setPassword(encryptedPassword);
        userRepo.save(user);
        cartService.createCartByUser(user);
        log.info("User registered!");
    }

    public User loginUser(UserCredentials userCredentials) throws Exception {

        User user = userRepo.findUserByPhone(userCredentials.getMobile());
        if(null == user){
            throw new Exception("User not found !");
        }
        String decryptedPassword = encryptDecrypt.decrypt(user.getPassword());
        if(!decryptedPassword.equals(userCredentials.getPassword())){
            throw new Exception("User entered wrong password");
        }
        return user;
    }

    public Address saveUserAddress(AddressModel addressModel){
        User user = userRepo.findUserByPhone(addressModel.getUserPhone());
        Address address = new Address();
        address.setIsDefault(true);
        address.setAddressOne(addressModel.getAddressOne());
        address.setArea(addressModel.getArea());
        address.setUser(user);
        address.setCity(addressModel.getCity());
        address.setLandmark(addressModel.getLandmark());
        address.setPincode(addressModel.getPincode());
        address.setMobile(addressModel.getMobile());
        Address recentDefaultAddress = addressRepo.save(address);
        List<Address> addresses = addressRepo.findAddressByUser(user);
        List<Address> listOfOtherAddress =  addresses.stream().filter(address1 -> !address1.getId().equals(recentDefaultAddress.getId()))
                        .map(add ->
                                new Address(add.getId(),add.getUser(),add.getAddressOne(),add.getLandmark(),add.getCity(),add.getArea(),add.getPincode(),
                                        add.getMobile(),false))
                                .collect(Collectors.toList());
        addressRepo.saveAll(listOfOtherAddress);
        return recentDefaultAddress;
    }

    public List<Address> getAddressByUser(String userPhone) {
        User user = userRepo.findUserByPhone(userPhone);
        return addressRepo.findAddressByUser(user);
    }
}
