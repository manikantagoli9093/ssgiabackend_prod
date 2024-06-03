package com.spring.rewards.response;

import java.util.Collection;

import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;


public class JwtResponse 
{
	private String token;
	private String type = "Bearer";
	private long empId;
	private String email;
	private List<GrantedAuthority> role;
	public JwtResponse(String token, long empId, String email, Collection<? extends GrantedAuthority> collection) {
		super();
		this.token = token;
		this.type = type;
		this.empId = empId;
		this.email = email;
		this.role = (List<GrantedAuthority>) collection;
	}
		// TODO Auto-generated constructor stub
	

	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public long getEmpId() {
		return empId;
	}
	public void setEmpId(long empId) {
		this.empId = empId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public List<GrantedAuthority> getRole() {
		return role;
	}
	public void setRole(GrantedAuthority role) {
		this.role = (List<GrantedAuthority>) role;
	}
	

}
