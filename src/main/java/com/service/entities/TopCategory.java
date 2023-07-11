package com.service.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "TOP_CATEGORY")
public class TopCategory {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CAT_ID")
    private Long catId;


    @Column(name = "PRODUCTS_COUNT")
    private Integer productCount;


}
