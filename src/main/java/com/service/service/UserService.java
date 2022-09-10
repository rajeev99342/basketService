package com.service.service;

import com.service.constants.enums.Role;
import com.service.entities.Address;
import com.service.entities.User;
import com.service.jwt.JwtTokenUtility;
import com.service.model.AddressModel;
import com.service.model.GlobalResponse;
import com.service.model.UserCredentials;
import com.service.repos.AddressRepo;
import com.service.repos.UserRepo;
import com.service.utilites.EncryptDecrypt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;

@Slf4j
@Service
public class UserService {

    @Autowired
    private PasswordEncoder bcryptEncoder;
    @Autowired
    CartService cartService;
    @Autowired
    UserRepo userRepo;
    @Autowired
    AddressRepo addressRepo;

    @Autowired
    JwtTokenUtility jwtTokenUtility;
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
        if(null != userCredentials.getRoles() && userCredentials.getRoles().size() > 0){
            user.setRoles(userCredentials.getRoles());
        }else{
            user.setRoles(new ArrayList<Role>(Collections.singleton(Role.CUSTOMER)));
        }
        user.setUserName(userCredentials.getName());
        user.setPhone(userCredentials.getMobile());
        user.setPassword(bcryptEncoder.encode(userCredentials.getPassword()));
        userRepo.save(user);
        cartService.createCartByUser(user);
        log.info("User registered!");
    }


    public User getUserByPhoneNumber(String phone){
        return userRepo.findUserByPhone(phone);
    }

    public void reset(UserCredentials userCredentials) throws Exception {

        User user = userRepo.findUserByPhone(userCredentials.getMobile());
        user.setUserName(userCredentials.getName());
        user.setPhone(userCredentials.getMobile());
        user.setPassword(bcryptEncoder.encode(userCredentials.getPassword()));
        userRepo.save(user);
        cartService.createCartByUser(user);
        log.info("password reset successfully");
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
        return  addressRepo.findAddressByUser(user);

    }

    public Address getAddressByUser(String userPhone) {
        User user = userRepo.findUserByPhone(userPhone);
        return addressRepo.findAddressByUser(user);
    }

    public UserCredentials isUserPresent(String userPhone){
        User user = userRepo.findUserByPhone(userPhone);
        UserCredentials userCredentials = new UserCredentials();
        if(null != user){
            userCredentials.setMobile(user.getPhone());
            userCredentials.setName(user.getUserName());
            return userCredentials;
        }else{
            return  null;
        }
    }

    public GlobalResponse updateUserToken(String token,String jwt) {
        User user = userRepo.findUserByPhone(jwtTokenUtility.getUsernameFromToken(jwt));
        if(null != user){
                user.setToken(token);
                userRepo.save(user);
                return new GlobalResponse("Token updated", HttpStatus.OK.value(),true,null);
        }else{
                return null;
        }
    }

}
