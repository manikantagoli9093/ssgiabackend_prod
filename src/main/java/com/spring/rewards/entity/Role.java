package com.spring.rewards.entity;

import jakarta.persistence.Entity;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
   
	@Enumerated(EnumType.STRING)
    private ERole name;
   
}
