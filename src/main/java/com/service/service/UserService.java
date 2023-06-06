package com.service.service;

import com.google.api.Http;
import com.service.constants.enums.UserRole;
import com.service.constants.enums.Status;
import com.service.entities.Address;
import com.service.entities.User;
import com.service.jwt.JwtTokenUtility;
import com.service.jwt.MyUserDetailsService;
import com.service.model.*;
import com.service.repos.AddressRepo;
import com.service.repos.UserRepo;
import com.service.utilites.EncryptDecrypt;
import com.service.utilites.UserFunction;
import com.service.utilites.UserUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import com.service.utilites.UserFunction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
@Slf4j
@Component
public class UserService {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    UserFunction userFunction;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtility jwtTokenUtil;
    @Autowired
    private PasswordEncoder bcryptEncoder;
    @Autowired
    CartService cartService;
    @Autowired
    UserRepo userRepo;
    @Autowired
    AddressRepo addressRepo;
    @Autowired
    MyUserDetailsService userDetailsService;
    @Autowired
    JwtTokenUtility jwtTokenUtility;
    @Autowired
    EncryptDecrypt encryptDecrypt;

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }



    public GlobalResponse signIn(UserCredentials loginDetails){
    GlobalResponse globalResponse = new GlobalResponse();
    try {

        authenticate(loginDetails.getMobile(), loginDetails.getPassword());

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(loginDetails.getMobile());

        final String token = jwtTokenUtil.generateToken(userDetails);
        UserModel userModel = new UserModel();
        userModel.setName(getUserByPhone(loginDetails.getMobile()).getUserName());
        userModel.setAddress(getAddressByUser(loginDetails.getMobile()));
        userModel.setPhone(loginDetails.getMobile());
        userModel.setJwt(token);
        List<String> roles = new ArrayList<>();
        for (GrantedAuthority grantedAuthority : userDetails.getAuthorities()) {
            roles.add(grantedAuthority.getAuthority());
        }
        User roleUser = userRepo.findUserByPhone(userModel.getPhone());
        userModel.setRoles(roles);
        userModel.setLoggedInAs(roleUser.getLoggedInAs());
        globalResponse.setMessage("Login successfully");
        globalResponse.setStatus(true);
        globalResponse.setHttpStatusCode(HttpStatus.OK.value());
        globalResponse.setBody(userModel);
    } catch (Exception e) {
        e.printStackTrace();
        globalResponse.setMessage(e.getMessage());
    }

    return globalResponse;
}
    public GlobalResponse saveUserDetails(UserCredentials userCredentials) {
        GlobalResponse globalResponse = new GlobalResponse();

        try{
            User user = userRepo.findUserByPhone(userCredentials.getMobile());
            if (!userCredentials.getPassword().equals(userCredentials.getConfirmedPassword())) {
                throw new Exception("Password doesn't matches");
            } else if (null != user) {
                throw new Exception("User already registered by this phone number, try to login");
            }

            user = new User();
            if (null != userCredentials.getRoles() && userCredentials.getRoles().size() > 0) {
                user.setRoles(userCredentials.getRoles());
            } else {
                user.setRoles(new ArrayList<UserRole>(Collections.singleton(UserRole.CUSTOMER)));
            }
            user.setUserName(userCredentials.getName());
            user.setPhone(userCredentials.getMobile());
            user.setLoggedInAs(UserRole.CUSTOMER);
            user.setPassword(bcryptEncoder.encode(userCredentials.getPassword()));
            userRepo.save(user);
            cartService.createCartByUser(user);
            globalResponse.setStatus(true);
            globalResponse.setMessage(Status.SUCCESS.toString());
        }catch (Exception e){
            e.printStackTrace();
            globalResponse.setMessage(e.getMessage());
        }

        return globalResponse;
    }


    public User getUserByPhoneNumber(String phone) {
        return userRepo.findUserByPhone(phone);
    }

    public GlobalResponse reset(UserCredentials userCredentials){
        GlobalResponse globalResponse = new GlobalResponse();
       try {
           globalResponse.setStatus(true);
           globalResponse.setMessage(Status.SUCCESS.toString());
           User user = userRepo.findUserByPhone(userCredentials.getMobile());
           user.setUserName(userCredentials.getName());
           user.setPhone(userCredentials.getMobile());
           user.setPassword(bcryptEncoder.encode(userCredentials.getPassword()));
           userRepo.save(user);
           cartService.createCartByUser(user);
           log.info("password reset successfully");
       }catch (Exception e){
           e.printStackTrace();
           globalResponse.setMessage(e.getMessage());
       }
       return globalResponse;
    }


    public User loginUser(UserCredentials userCredentials) throws Exception {

        User user = userRepo.findUserByPhone(userCredentials.getMobile());
        if (null == user) {
            throw new Exception("User not found !");
        }
        String decryptedPassword = encryptDecrypt.decrypt(user.getPassword());
        if (!decryptedPassword.equals(userCredentials.getPassword())) {
            throw new Exception("User entered wrong password");
        }
        return user;
    }

    public GlobalResponse saveUserAddress(AddressModel addressModel) {

        GlobalResponse globalResponse = new GlobalResponse();
//        try {
//            User user = userRepo.findUserByPhone(addressModel.getMobile());
//            Address address = new Address();
//            if(null != addressModel.getId()){
//                address.setId(addressModel.getId());
//            }
//            Address savedAddress = addressRepo.save(address);
//            globalResponse.setStatus(true);
//            globalResponse.setBody(savedAddress);
//            globalResponse.setMessage(Status.SUCCESS.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//            globalResponse.setMessage("Failed");
//        }

        return globalResponse;


    }

    public AddressModel getAddressByUser(String userPhone) {
        User user = userRepo.findUserByPhone(userPhone);
        return userFunction.CONVERT_INTO_MODEL.apply(addressRepo.findAddressByUser(user));
    }

    public UserCredentials isUserPresent(String userPhone) {
      try{
          User user = userRepo.findUserByPhone(userPhone);
          UserCredentials userCredentials = new UserCredentials();
          if (null != user) {
              userCredentials.setMobile(user.getPhone());
              userCredentials.setName(user.getUserName());
              return userCredentials;
          } else {
              return null;
          }
      }catch (Exception e){
          return null;
      }
    }


    public User getUserByPhone(String userPhone) {
        try {
            User user = userRepo.findUserByPhone(userPhone);
            if (null != user) {
                return user;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public GlobalResponse updateUserToken(String token, String jwt) {
        try {
            User user = userRepo.findUserByPhone(jwtTokenUtility.getUsernameFromToken(jwt));
            if (null != user) {
                user.setToken(token);
                userRepo.save(user);
                return new GlobalResponse("Token updated", HttpStatus.OK.value(), true, null);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }


    public Status updateUserName(String username, Authentication authentication) {

        try{
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userPhone = userDetails.getUsername();
            User user = userRepo.findUserByPhone(userPhone);
            user.setUserName(username);
            userRepo.save(user);
            return Status.SUCCESS;
        }catch (Exception e){
            e.printStackTrace();
        }
        return Status.FAILED;

    }

    public GlobalResponse signInAs(UserRole role,String phone , Authentication authentication) {
        User user = userRepo.findUserByPhone(phone);
        user.setLoggedInAs(role);
        userRepo.save(user);
        log.info("{} is logged in",phone);
        return new GlobalResponse(String.format("%s is logged in as : %s",phone,role.name()), HttpStatus.OK.value());
    }

    public GlobalResponse updateLocation(AddressModel addressModel,Authentication authentication) {
        try{
            Address address ;
            User user = userRepo.findUserByPhone(addressModel.getUserPhone());
            Address alreadySavedAddress = addressRepo.findAddressByUser(user);
            if(alreadySavedAddress != null){
                address = alreadySavedAddress;
            }else{
                address = new Address();
            }
            address.setAddressLine(addressModel.getAddressLine());
            address.setCompleteAddress(addressModel.getCompleteAddress());
            address.setLatitude(addressModel.getLatitude());
            address.setLongitude(addressModel.getLongitude());
            address.setUser(user);
            addressRepo.save(address);
            return new GlobalResponse("Location updated", 200,true,addressModel);
        }catch(Exception ex){
            return new GlobalResponse("Failed to update location : "+ex.getMessage(), 401);
        }
    }

    public GlobalResponse getUserByRole(UserRole role) {
        GlobalResponse response = new GlobalResponse();
        List<User> users = null;
        try{
            List<String> roles = new ArrayList<>();
            roles.add(UserRole.DELIVERY.name());
            users = userRepo.findByRolesContains(role);
            response.setHttpStatusCode(HttpStatus.OK.value());
            response.setStatus(true);
            response.setBody(userFunction.CONVERT_INTO_USER_MODEL_LIST.apply(users));
        }catch (Exception e){
            log.error("deliver agent fetch failed due to : ",e);
            response.setStatus(false);
            response.setMessage(e.getMessage());
            response.setHttpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }
}
