package com.service.repos;

import com.service.entities.ImageDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@NoRepositoryBean
public interface ImageDetailsRepository extends JpaRepository<ImageDetails,Long> {
    /**
     *
     */
//    ImageDetails findAllBImageDetailsId(Long id);
//    find
//    public ImageDetails find


}
