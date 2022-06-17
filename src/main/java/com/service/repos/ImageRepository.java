package com.service.repos;

import com.service.entities.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image,Long> {
    public Image findImageByCategoryId(Long id);
    public List<Image> findImageByProductId(Long id);
}
