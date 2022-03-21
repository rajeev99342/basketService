package com.service.entities;

import com.service.constants.PAYMENTMODE;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "IMAGE_DETAILS")
public class ImageDetails {
    @Id
    @Column(name = "IMAGE_DETAILS_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME")
    private String imageName;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "PATH")
    private String path;
}
