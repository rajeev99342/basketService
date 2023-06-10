package com.service.repos;

import com.service.constants.enums.UserRole;
import com.service.entities.User;
import com.service.model.interfacemodel.ICountAmount;
import com.service.model.interfacemodel.IUserModel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {

    public User findUserByPhone(String phone);

    @Query(value = "select token from user as u where u.crnt_user_type = :crnt_user_type",nativeQuery = true)
    List<String> findToken(@Param("crnt_user_type") String crnt_user_type);


    @Query(value = "select token from `user` as u where u.ROLES in (:ROLE) ",nativeQuery = true)
    List<User> findUserByRole(@Param("ROLE") List<String> ROLE);

    List<User> findByRolesContains(UserRole role, Pageable pageable);// fetch by user role

    @Query(value = "SELECT u.USER_NAME as name, GROUP_CONCAT(r.roles) as roles, a.ADDRESS_LINE as address, u.lat as lat, u.lon as lon, u.phone as phone FROM User u JOIN User_roles r ON u.USER_ID = r.User_USER_ID JOIN Address a ON u.USER_ID = a.USER_ID GROUP BY u.USER_NAME, a.ADDRESS_LINE,u.lat, u.lon, u.phone",nativeQuery = true)
    List<IUserModel> findUserSummary(Pageable pageable);

    @Query(value = "SELECT u.USER_NAME as name, GROUP_CONCAT(r.roles) as roles, a.ADDRESS_LINE as address, u.lat as lat, u.lon as lon, u.phone as phone FROM User u JOIN User_roles r ON u.USER_ID = r.User_USER_ID JOIN Address a ON u.USER_ID = a.USER_ID where r.roles = :role GROUP BY u.USER_NAME, a.ADDRESS_LINE,u.lat, u.lon, u.phone",nativeQuery = true)
    List<IUserModel> findUserSummaryByRole(@Param("role") String role,Pageable pageable);
}
