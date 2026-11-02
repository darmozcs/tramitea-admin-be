package com.darmoz.tramiteaadmin.repository;

import com.darmoz.tramiteaadmin.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);
}
