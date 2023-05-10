package com.service.utilites;

import com.service.constants.enums.UserRole;
import com.service.entities.Address;
import com.service.entities.User;
import com.service.model.AddressModel;
import com.service.model.LocationCord;
import com.service.model.UserModel;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UserFunction {
    public Function<Address, AddressModel> CONVERT_INTO_MODEL = address -> {
        if(address != null){
            AddressModel model = new AddressModel();
            model.setId(address.getId());
            model.setUserPhone(address.getUser().getPhone());
            model.setLatitude(address.getLatitude());
            model.setLongitude(address.getLongitude());
            model.setCompleteAddress(address.getCompleteAddress());
            model.setAddressLine(address.getAddressLine());
            return model;
        }
        return null;

    };


    public Function<User, UserModel> CONVERT_INTO_USER_MODEL = user -> {
        if(user != null){
            final String token = user.getToken();
            UserModel userModel = new UserModel();
            userModel.setName(user.getUserName());
            userModel.setPhone(user.getPhone());
            userModel.setJwt(token);
            List<String> roles = user.getRoles().stream().map(role->role.name()).collect(Collectors.toList());
//            User roleUser = userRepo.findUserByPhone(userModel.getPhone());
            userModel.setRoles(roles);
            userModel.setLoggedInAs(user.getLoggedInAs());
            userModel.setLocationCord(new LocationCord(user.getLat(),user.getLon()));
            return userModel;
        }
        return null;

    };
}
