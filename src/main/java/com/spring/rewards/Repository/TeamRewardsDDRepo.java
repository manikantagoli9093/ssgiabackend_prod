package com.spring.rewards.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.rewards.entity.TeamRewardsDropDown;

public interface TeamRewardsDDRepo extends JpaRepository<TeamRewardsDropDown, Long> {

}
