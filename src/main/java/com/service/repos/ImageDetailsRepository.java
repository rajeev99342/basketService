package com.service.repos;

import com.service.entities.ImageDetails;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageDetailsRepository extends JpaRepository<ImageDetails,Long> {
    /**
     *
     */
    @Override
    List<ImageDetails> findAll();

    ImageDetails findImageDetailsById(Long id);
    //    public List<ImageDetails> findByName(String name);
    //    ImageDetails findAllBImageDetailsId(Long id);
//    find
//    public ImageDetails find


}
