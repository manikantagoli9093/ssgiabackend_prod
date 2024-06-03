package com.spring.rewards.security.services;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.spring.rewards.Repository.EmployeeRepository;
import com.spring.rewards.Repository.UserRepository;
import com.spring.rewards.entity.Employee;
import com.spring.rewards.entity.User;

import jakarta.transaction.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
	
	 @Autowired
	UserRepository userRepo;
	 
	@Autowired
	EmployeeRepository employeeRepo;
     
	 @Override
	 @Transactional
      public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
	    User user = userRepo.findByEmail(email);
	    		if (user==null) {throw new UsernameNotFoundException("User not found with email: " + email);
	    }
	    Employee employee=user.getEmployee();
	    		if(employee==null) {
	    			throw  new UsernameNotFoundException("Employee not found with thr email:"+email);
	    		}

		return UserDetailsImpl.build(employee,user);
	  }
	
	
	
}
