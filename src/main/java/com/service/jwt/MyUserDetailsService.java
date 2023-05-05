package com.service.jwt;

import com.service.constants.enums.UserRole;
import com.service.entities.User;
import com.service.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepo userRepo;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userPhone) throws UsernameNotFoundException {
        User user = userRepo.findUserByPhone(userPhone);
        List<UserRole> roles = user.getRoles();
        List<GrantedAuthority> authorities = new ArrayList<>();
        for(UserRole role : roles){
            GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(role.name());
            authorities.add(grantedAuthority);
        }

        return new org.springframework.security.core.userdetails.User(user.getPhone(),
                user.getPassword(), authorities);

    }
}
