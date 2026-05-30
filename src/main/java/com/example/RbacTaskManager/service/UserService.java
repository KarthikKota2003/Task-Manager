package com.example.RbacTaskManager.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.RbacTaskManager.enums.Role;
import com.example.RbacTaskManager.exceptions.UnauthorizedUserException;
import com.example.RbacTaskManager.exceptions.UserNotFoundException;
import com.example.RbacTaskManager.model.User;
import com.example.RbacTaskManager.repository.TaskRepository;
import com.example.RbacTaskManager.repository.UserRepository;

import jakarta.annotation.PostConstruct;

@Service
public class UserService
{
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private TaskRepository taskRepository;
	
	public UserService()
	{
		
	}
	
	@PostConstruct
	public void createFirstUser()
	{
		if(userRepository.count()==0)
		{
			User user=new User();
			user.setUserName("manager1");
			user.setPassword("manager1");  // Plain text, no encoding
			user.setRole(Role.MANAGER);
			userRepository.save(user);
			System.out.println("Initial manager user created with username: manager1 and password: manager1");
		}
	}
	
	public User getUserById(int uId)
	{
		Optional<User> user=userRepository.findById(uId);
		if(user.isEmpty())
		{
			throw new UserNotFoundException("User not Found with id: "+uId);
		}
		return user.get();
	}
	
	public User createUser(int uId, User user)
	{
		User manager=getUserById(uId);
		if(manager.getRole()==Role.EMPLOYEE)
		{
			throw new UnauthorizedUserException("Employee cannot create user.");
		}
		if(user.getManager()==null || user.getManager().getId()!=manager.getId())
		{
			throw new UnauthorizedUserException("Wrong Manager was set.");
		}
		user.setManager(manager);
		user.setPassword(user.getPassword());  // Plain text, no encoding
		User newUser = userRepository.save(user);
		return newUser;
	}
	
	public List<User> getAllEmployees(int uId)
	{
		User user=getUserById(uId);
		if(user.getRole()==Role.EMPLOYEE)
		{
			throw new UnauthorizedUserException("Employee cannot create user.");
		}
		List<User> employees=userRepository.findByManager(user);
		if(employees==null || employees.isEmpty())
		{
			employees=new ArrayList<>();
			employees.add(user);
		}
		return employees;
	}
	
	public User deleteUser(int uId, int dId)
	{
		User manager=getUserById(uId);
		User user=getUserById(dId);
		if(manager.getRole()==Role.EMPLOYEE)
		{
			throw new UnauthorizedUserException("Employee cannot delete user.");
		}
		if(user.getManager()==null || user.getManager().getId()!=manager.getId() || uId==dId)
		{
			throw new UnauthorizedUserException("Only Employee Manager can delete");
		}
		boolean hasEmployees=userRepository.existsByManagerId(dId);
		if(hasEmployees)
		{
			throw new UnauthorizedUserException("Employee cannot be deleted because this employee is a manager for other employees.");
		}
		boolean hasTasks=taskRepository.existsByCreatedByIdOrAssignedToId(dId, dId);
		if(hasTasks)
		{
			throw new UnauthorizedUserException("Employee cannot be deleted because this employee is assigned to tasks.");
		}
		userRepository.deleteById(user.getId());
		return user;
	}
}