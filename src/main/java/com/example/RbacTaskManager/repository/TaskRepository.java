package com.example.RbacTaskManager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RbacTaskManager.model.Task;
import com.example.RbacTaskManager.model.User;

public interface TaskRepository extends JpaRepository<Task,Integer>
{
	List<Task> findByAssignedTo(User user);
	List<Task> findByCreatedBy(User user);
	boolean existsByCreatedByIdOrAssignedToId(int createdById, int assignedToId);
}
