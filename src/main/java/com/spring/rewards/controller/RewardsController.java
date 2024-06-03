package com.spring.rewards.controller;

import java.util.List;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring.rewards.entity.RewardType;
import com.spring.rewards.entity.Rewards;
import com.spring.rewards.entity.RewardsDropDown;
import com.spring.rewards.services.RewardsService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins="*")
public class RewardsController {
	
	@Autowired
	private RewardsService rewardsService;
	
	
	@PostMapping("submit/{empId}")
	public ResponseEntity<Object> submitRewards(@PathVariable Long empId, @RequestBody Rewards rewards) {
	    try {
	        Rewards reward = rewardsService.submitRewards(empId, rewards);
	        if (reward != null) {
	            return ResponseEntity.ok(reward);
	        } else {
	            return ResponseEntity.notFound().build();
	        }
	    } catch (EntityNotFoundException  e) {
	        e.printStackTrace(); 
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	    } catch (Exception e) {
	        e.printStackTrace(); 
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}
	
	@PutMapping("/approveOrReject/{empId}")
    public ResponseEntity<Object> approveOrRejectRewards(
            @PathVariable Long empId,
            @RequestBody Map<String, Object> requestBody) {

        try {
        	
            long rewardId = ((Number) requestBody.get("id")).longValue();
            boolean approve = (boolean) requestBody.get("approve");

            Rewards reward = rewardsService.approveOrRejectRewards(empId, rewardId, approve);
            
            if (reward != null) {
                return ResponseEntity.ok(reward);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
	

    @GetMapping("/requests/{empId}")
    public ResponseEntity<Object> getRewardRequestsForParent(@PathVariable Long empId) {

        try {
            List<Rewards> rewardRequests = rewardsService.getRewardRequestsForParent(empId);

            if (!rewardRequests.isEmpty()) {
                return ResponseEntity.ok(rewardRequests);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    
    
    @GetMapping("/all")
    public List<RewardsDropDown> getAllRewardsDropDown() {
        return rewardsService.getAllRewardsDropDown();
    }
   
    @GetMapping("/rewardTypes")
    public List<RewardType>getAllRewardsTypeDropDown() throws NullPointerException {
        return rewardsService.getAllRewardsTypeDropDown();
    }
    
//    @GetMapping("/rewardTypes/{id}")
//    public Optional<RewardType> getAllRewardsTypeDropDownId(@PathVariable Long id) {
//      
//    	Optional<RewardType> rT= rewardsService.getAllRewardsTypeDropDown(id);
//    	return rT;
//    }
//    
    
     
    @GetMapping("/myrequests/{empId}")
    public ResponseEntity<Object> getRewardForUser(@PathVariable Long empId) {

        try {
            List<Rewards> rewardRequests = rewardsService.getRewardsForUser(empId);

            if (!rewardRequests.isEmpty()) {
                return ResponseEntity.ok(rewardRequests);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/getname/{empId}")
    public String getEmpName(@PathVariable Long empId) {
    	return rewardsService.getEmpName(empId);
    }
    
    
    
    @GetMapping("/allrewards")
    public ResponseEntity<Object> getAllRewrdsForDashboard() {

        try {
            Map<Long, Map<String, Integer>> rewardRequests = rewardsService.getAllRewardsForDashboard();

            if (!rewardRequests.isEmpty()) {
                return ResponseEntity.ok(rewardRequests);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    
    
    @GetMapping("/allrewards2")
    public ResponseEntity<Object> getAllRewrdsForDashboard2() {

        try {
          List<Rewards> rewardRequests = rewardsService.getAllRewardsForDash();

            if (!rewardRequests.isEmpty()) {
                return ResponseEntity.ok(rewardRequests);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    
    
    
    
    
    
    

}
