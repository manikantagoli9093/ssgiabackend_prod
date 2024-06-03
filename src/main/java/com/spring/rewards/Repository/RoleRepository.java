package com.spring.rewards.Repository;

import java.util.Optional;





import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.spring.rewards.entity.ERole;
import com.spring.rewards.entity.Role;


@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
	 
	Optional<Role> findByName(ERole name);

}
