package com.service.repos;

import com.service.constants.enums.UserRole;
import com.service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {

    public User findUserByPhone(String phone);

    @Query(value = "select token from user as u where u.crnt_user_type = :crnt_user_type",nativeQuery = true)
    List<String> findToken(@Param("crnt_user_type") String crnt_user_type);


    @Query(value = "select token from `user` as u where u.ROLES in (:ROLE) ",nativeQuery = true)
    List<User> findUserByRole(@Param("ROLE") List<String> ROLE);

    List<User> findByRolesContains(UserRole role);



}
