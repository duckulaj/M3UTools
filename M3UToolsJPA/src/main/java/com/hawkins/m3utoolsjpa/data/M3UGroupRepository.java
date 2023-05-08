package com.hawkins.m3utoolsjpa.data;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;


public interface M3UGroupRepository extends JpaRepository<M3UGroup, Long> {
	
	
	M3UGroup findById(long id);

	List<M3UGroup> findByType(String type, Sort sort);
	
	M3UGroup findByName(String name);
	
	M3UGroup findById(long id, Sort sort);

	
	
	
}
