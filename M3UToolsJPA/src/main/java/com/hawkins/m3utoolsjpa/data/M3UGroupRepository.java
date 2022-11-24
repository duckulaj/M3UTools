package com.hawkins.m3utoolsjpa.data;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface M3UGroupRepository extends PagingAndSortingRepository<M3UGroup, Long> {

	
	M3UGroup findById(long id);

	M3UGroup findByType(String type);
	
	M3UGroup findByName(String name);
	
	M3UGroup findById(long id, Sort sort);

}
