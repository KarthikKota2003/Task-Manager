package com.example.RbacTaskManager.model;

import java.util.List;

import com.example.RbacTaskManager.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity(name="users")
public class User {
	@Id
	@GeneratedValue
	private int id;
	private String userName;
	private String password;
	private Role role;
	@ManyToOne
	@JoinColumn(name="manager")
	private User manager;
	@JsonIgnore
	@OneToMany(mappedBy="manager")
	private List<User> employees;
	@JsonIgnore
	@OneToMany(mappedBy="assignedTo")
	private List<Task> assignedTasks;
	@JsonIgnore
	@OneToMany(mappedBy="createdBy")
	private List<Task> createdTasks;
	
	public User()
	{
		
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
	public User getManager() {
		return manager;
	}
	public void setManager(User manager) {
		this.manager = manager;
	}
	public List<User> getEmployees() {
		return employees;
	}
	public void setEmployees(List<User> employees) {
		this.employees = employees;
	}
	public List<Task> getAssignedTasks() {
		return assignedTasks;
	}
	public void setAssignedTasks(List<Task> assignedTasks) {
		this.assignedTasks = assignedTasks;
	}
	public List<Task> getCreatedTasks() {
		return createdTasks;
	}
	public void setCreatedTasks(List<Task> createdTasks) {
		this.createdTasks = createdTasks;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
