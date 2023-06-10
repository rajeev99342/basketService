package com.service.entities;

import com.service.constants.enums.UserRole;
import com.service.constants.enums.UserType;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "USER")
public class User extends BaseEntity {
    @Id
    @Column(name = "USER_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "USER_NAME")
    private String userName;


    @Enumerated(EnumType.STRING)
    @Column(name = "CRNT_USER_TYPE")
    private UserRole loggedInAs;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "TOKEN")
    private String token;

    @Column(name = "ROLES")
    @ElementCollection(targetClass = UserRole.class)
    @Enumerated(EnumType.STRING)
    private List<UserRole> roles;

    @Column(name = "lat")
    private String lat;

    @Column(name = "lon")
    private String lon;

}
