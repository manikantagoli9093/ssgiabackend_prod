package com.spring.rewards.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RewardsDropDown {
	@Id
	@Column(name = "rewards_dropdown_id")
	private long id;
	private String rewardPoints;
	private String rewardName;
	private String rewardType;
	

}
