package com.spring.rewards.request;

import java.util.Set;

import com.spring.rewards.entity.ERole;





public class LoginRequest {
	
	private String email;
	private String password;
	private Set<ERole> roles;

	public Set<ERole> getRoles() {
		return roles;
	}

	public void setRoles(Set<ERole> roles) {
		this.roles = roles;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
