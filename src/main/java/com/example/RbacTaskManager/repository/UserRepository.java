package com.example.RbacTaskManager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RbacTaskManager.model.User;

public interface UserRepository extends JpaRepository<User,Integer>
{
	List<User> findByManager(User manager);
	boolean existsByManagerId(int managerId);
	User findByUserName(String userName);
}
