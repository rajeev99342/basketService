package com.service.entities;

import com.service.constants.UserType;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "USER")
public class User {
    @Id
    @Column(name = "USER_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "USER_TYPE")
    private UserType userType;

    @Column(name = "PHONE")
    private String phone;

}
