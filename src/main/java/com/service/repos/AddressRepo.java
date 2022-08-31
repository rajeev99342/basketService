package com.service.repos;

import com.service.entities.Address;
import com.service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepo extends JpaRepository<Address,Long> {
    Address findAddressByUser(User user);
    Address findAddressByUserAndIsDefault(User user,Boolean isDefault);

}
