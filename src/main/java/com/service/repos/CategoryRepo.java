package com.service.repos;

import com.service.entities.Category;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepo extends JpaRepository<Category,Long> {
    List<Category> findCategoryByIsValid(Boolean isValid);
    Category findByIdAndIsValid(Long id,Boolean isValid);
}
