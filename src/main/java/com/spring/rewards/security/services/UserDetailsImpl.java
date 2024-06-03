package com.spring.rewards.security.services;

import java.util.Collection;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spring.rewards.entity.Employee;
import com.spring.rewards.entity.User;

import jakarta.persistence.Column;

public class UserDetailsImpl implements UserDetails {


	private long empId;

	private String empName;

	
	@Column(name = "email", unique = true)
	private String email;

	@JsonIgnore
	private String password;

	

	private List<GrantedAuthority> authority;


	public UserDetailsImpl(long empId, String empName, String email, String password, List<GrantedAuthority> authorities) {
		super();
		this.empId = empId;
		this.empName = empName;
		this.email = email;
		this.password = password;
		this.authority = authorities;
	}

	public static UserDetailsImpl build(Employee employee,User user) {
		List<GrantedAuthority> authorities = employee.getRoles().stream()
				.map(role -> new SimpleGrantedAuthority(role.getName().name())).collect(Collectors.toList());
		return new UserDetailsImpl(employee.getEmpId(), employee.getEmpName(), user.getEmail(), user.getPassword(), authorities);
		

	}

	public List<GrantedAuthority> getAuthority() {
		return authority;
	}

	public long getEmpId() {
		return empId;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String getPassword() {
		return password;
	}


	public String getEmpName() {
		return empName;
	}

	public void setEmpName(String empName) {
		this.empName = empName;
	}

	

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setAuthority(List<GrantedAuthority> authority) {
		this.authority = authority;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return null;
	}
}
