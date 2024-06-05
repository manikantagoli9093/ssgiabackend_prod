package com.spring.rewards.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.spring.rewards.Repository.EmployeeRepository;
import com.spring.rewards.Repository.RewardTypeRepo;
import com.spring.rewards.Repository.RewardsDropDownRepo;
import com.spring.rewards.Repository.RewardsRepository;
import com.spring.rewards.entity.Employee;
import com.spring.rewards.entity.RewardType;
import com.spring.rewards.entity.Rewards;
import com.spring.rewards.entity.RewardsDropDown;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.transaction.annotation.Transactional;


@Service
public class RewardsService {
	
	@Autowired
	private RewardsRepository rewardRepo;
	
	@Autowired
	private RewardsDropDownRepo rewardsDDR;
	
	@Autowired
	private RewardTypeRepo rewardtypeRepo;
	
	@Autowired
	private EmployeeRepository empRepo;

	public Rewards submitRewards(long empId,Rewards rewards ) {
		Optional<Employee> employeeOptional= empRepo.findById(empId);
		
		if(employeeOptional.isEmpty())
		{
			throw new EntityNotFoundException("employee with id " + empId + " does not exist.");
		}
		Employee childEmployee= employeeOptional.get();
		
		Employee parentEmployee= childEmployee.getParent();
		if(parentEmployee==null) {
			throw new EntityNotFoundException("parent employee doesnot exit for the employee with the id"+empId);
		}
		Rewards reward=new Rewards();
		reward.setEmpId(empId);
		reward.setRewards(rewards.getRewards());
		reward.setComments(rewards.getComments());
		reward.setStatus("Pending");
		parentEmployee.addReward(reward);
		reward.setEmpName(childEmployee.getEmpName());
		empRepo.save(parentEmployee);
		 
		return reward;
		
		
     	    }
	 public Rewards approveOrRejectRewards(long empId, long rewardId, boolean approve, String remarks) {
	        Optional<Employee> employeeOptional = empRepo.findById(empId);

	        if (employeeOptional.isEmpty()) {
	            throw new EntityNotFoundException("Employee with id " + empId + " does not exist.");
	        }

	        Employee parentEmployee = employeeOptional.get();

	        Optional<Rewards> rewardOptional = parentEmployee.getRewards().stream()
	                .filter(reward -> reward.getId() == rewardId)
	                .findFirst();

	        if (rewardOptional.isEmpty()) {
	            throw new EntityNotFoundException("Reward with id " + rewardId + " does not exist for the employee with id " + empId);
	        }

	        Rewards reward = rewardOptional.get();
	        reward.setStatus(approve ? "Approved" : "Rejected");
	        reward.setRemarks(remarks);
	        empRepo.save(parentEmployee);

	        return reward;
	    }
	 
	 
	 public List<Rewards> getRewardRequestsForParent(long empId) {
	        Optional<Employee> employeeOptional = empRepo.findById(empId);

	        if (employeeOptional.isEmpty()) {
	            throw new EntityNotFoundException("Employee ;;with id " + empId + " does not exist.");
	        }

	        Employee parentEmployee = employeeOptional.get();

	        return parentEmployee.getRewards().stream()
	                .filter(reward -> "Pending".equals(reward.getStatus()))
	                .collect(Collectors.toList());
	    }
	 
	 
	 
	 
	 public List<RewardsDropDown> getAllRewardsDropDown() {
	        return rewardsDDR.findAll();
	    }
	 
	 public List<RewardType> getAllRewardsTypeDropDown() {
		    return rewardtypeRepo.findAll();
	        
	    }
	 
	 
	
	 
	 
	 public List<Rewards> getRewardsForUser(long empId) {
		    return rewardRepo.findByEmpId(empId);
		}
	 public Map<Long, Map<String, Integer>>getAllRewardsForDashboard(){
		 
		 List<Rewards> allRewards = rewardRepo.findAll();
		 Map<Long,Map<String,Integer>> employeePointsMap= new HashMap<>();
		 for(Rewards reward: allRewards) {
			 long empId=reward.getEmpId();
			 
			 String rewardType=reward.getRewards().getRewardType();
			 String rewardPoints= reward.getRewards().getRewardPoints();
			
			  int parsedRewardPoints;
		        try {
		        	parsedRewardPoints = Integer.parseInt(rewardPoints);
		        } catch (NumberFormatException e) {
		        	parsedRewardPoints = 0; 
		        }
			 
			 
			 
			 
			 employeePointsMap.computeIfAbsent(empId,K -> new HashMap<>());
			 
			 
			 Map<String,Integer> pointsMap = employeePointsMap.get(empId);
			 pointsMap.put(rewardType,pointsMap.getOrDefault(parsedRewardPoints, 0)+parsedRewardPoints);
			 
			 }
		 return employeePointsMap;
		 
		 
		 		 
	 }
	 
	 public List<Rewards> getAllRewardsForDash(){
		 
		 return rewardRepo.findAll();
	 }
	 
	 public String getEmpName(long empId) {
		 Optional<Employee> optEmp= empRepo.findById(empId);
		 if(optEmp.isEmpty()) {
			 throw new EntityNotFoundException("Employee with id " + empId + " does not exist.");
		 }
		 Employee employee= optEmp.get();
		 return employee.getEmpName();
		 
		 
	 }
	 
	

}
