package com.spring.rewards.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class RewardType {
	
	@Id
	@Column(name="reward_type_id")
	private Long id;
	private String rewardType;
	

}
