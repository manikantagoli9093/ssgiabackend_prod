package com.spring.rewards.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="users")
@Data
public class User 
{
	@Id
	@Column(name="user_id")
	private Long userId;
	
    @Column(name = "email", unique = true)
	private String email;
	private String password;
	
	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("user")
     private Employee employee;
	 
	 
	public User(String email, String password) {
		super();
		this.email = email;
		this.password = password;
	}
	
	public User() {
		super();
	}
	public void setActiveStatus(boolean b) {
		// TODO Auto-generated method stub
		
	}
	
	

}
