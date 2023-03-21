package com.hawkins.m3utoolsjpa.data;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FilterRepository extends JpaRepository<Filter, Long> {

	
	Filter findById(long id);

	Filter findByName(String name);
	
	Filter findById(long id, Sort sort);

	Filter save(Filter filter);
}
