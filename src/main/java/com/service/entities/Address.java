package com.service.entities;

import lombok.Data;

import javax.persistence.*;


@Data
@Entity
@Table(name = "ADDRESS")
public class Address {
    @Id
    @Column(name = "ADDRESS_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID",referencedColumnName = "USER_ID")
    private User user;

    @Column(name = "ADDRESS_ONE")
    private String addressOne;

    @Column(name = "LANDMARK")
    private String landMark;

    @Column(name = "CITY")
    private String city;

    @Column(name = "AREA")
    private String area;

    @Column(name = "PIN")
    private Long pin;



}
