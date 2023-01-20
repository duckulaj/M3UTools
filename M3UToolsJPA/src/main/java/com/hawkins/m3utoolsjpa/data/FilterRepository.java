package com.hawkins.m3utoolsjpa.data;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilterRepository extends PagingAndSortingRepository<Filter, Long> {

	
	Filter findById(long id);

	Filter findByName(String name);
	
	Filter findById(long id, Sort sort);

}
