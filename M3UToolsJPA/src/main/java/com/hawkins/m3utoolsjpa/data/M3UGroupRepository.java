package com.hawkins.m3utoolsjpa.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;


public interface M3UGroupRepository extends JpaRepository<M3UGroup, Long> {
	
	@Autowired
    static JdbcTemplate jdbcTemplate = new JdbcTemplate();

    @Transactional
    public static void truncateTable() {
        String sql = "TRUNCATE TABLE M3UGROUP";
        jdbcTemplate.execute(sql);
    }
	
	M3UGroup findById(long id);

	List<M3UGroup> findByType(String type, Sort sort);
	
	M3UGroup findByName(String name);
	
	M3UGroup findById(long id, Sort sort);
	
	M3UGroup findByCategoryid(String categoryid);

	
	
	
}