package com.service.repos;

import com.service.entities.Address;
import com.service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepo extends JpaRepository<Address,Long> {
    List<Address> findAddressByUser(User user);

}
