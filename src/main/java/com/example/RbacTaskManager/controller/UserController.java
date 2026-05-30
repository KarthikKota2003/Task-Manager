package com.example.RbacTaskManager.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RbacTaskManager.model.User;
import com.example.RbacTaskManager.security.CustomUserDetails;
import com.example.RbacTaskManager.service.UserService;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserController
{
	@Autowired
	private UserService userService;
	
	@GetMapping("/employee/getMe")
	public ResponseEntity<User> getUserById(@AuthenticationPrincipal CustomUserDetails userDetails)
	{
		User user = userDetails.getUser();
		int uId = user.getId();
		return ResponseEntity.ok(userService.getUserById(uId));
	}
	
	@GetMapping("/manager/get-all")
	public ResponseEntity<List<User>> getAllEmployees(@AuthenticationPrincipal CustomUserDetails userDetails)
	{
		User user = userDetails.getUser();
		int uId = user.getId();
		return ResponseEntity.ok(userService.getAllEmployees(uId));
	}
	
	@PostMapping("/manager/create")
	public ResponseEntity<User> postUser(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody User user)
	{
		User loggedInUser = userDetails.getUser();
		int uId = loggedInUser.getId();
		return ResponseEntity.ok(userService.createUser(uId,user));
	}
	
	@DeleteMapping("/manager/delete/{dId}")
	public ResponseEntity<User> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable int dId)
	{
		User user = userDetails.getUser();
		int uId = user.getId();
		return ResponseEntity.ok(userService.deleteUser(uId,dId));
	}
}
