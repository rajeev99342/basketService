package com.service.jwt;

import com.service.constants.enums.Role;
import com.service.entities.User;
import com.service.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String userPhone) throws UsernameNotFoundException {
            User user = userRepo.findUserByPhone(userPhone);
            GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("USER");
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(grantedAuthority);
            return new org.springframework.security.core.userdetails.User(user.getPhone(),
                    user.getPassword(), authorities);

    }
}
