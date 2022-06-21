package com.service.repos;

import com.service.entities.CartProductMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartProductRepo extends JpaRepository<CartProductMapping,Long> {
}
