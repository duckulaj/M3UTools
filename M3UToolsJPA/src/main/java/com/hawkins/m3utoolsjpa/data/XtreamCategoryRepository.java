package com.hawkins.m3utoolsjpa.data;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface XtreamCategoryRepository extends JpaRepository<XtreamCategory, Long> {
	
	Optional<XtreamCategory> findById(Long id);
	
	Optional<XtreamCategory> findByCategoryId(Long category_id);
	
	Optional<XtreamCategory> findByCategoryName(String category_name);
	
	Optional<XtreamCategory> findByType(String type);

}