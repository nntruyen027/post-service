package com.qbit.microservice.repository;

import com.qbit.microservice.entity.ProductFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, Long> {
}
