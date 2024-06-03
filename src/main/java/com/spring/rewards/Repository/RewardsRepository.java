package com.spring.rewards.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.spring.rewards.entity.Rewards;


@Repository
public interface RewardsRepository extends JpaRepository<Rewards, Long> {

	List<Rewards> findByEmpId(long empId);

	
	
}
