package com.service.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "CATEGORY")
public class Category {
    @Id
    @Column(name = "CAT_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CAT_NAME")
    private Long catName;

    @Column(name = "CAT_TYPE")
    private Long catType;

}
