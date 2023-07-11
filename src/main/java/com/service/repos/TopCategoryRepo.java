package com.service.repos;

import com.service.entities.Address;
import com.service.entities.TopCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopCategoryRepo  extends JpaRepository<TopCategory,Long> {
}
