package com.spring.rewards.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.rewards.entity.RewardType;

public interface RewardTypeRepo extends JpaRepository<RewardType, Long> {
	
	List<RewardType> findAll();

}
