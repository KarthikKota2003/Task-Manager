package com.example.RbacTaskManager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.RbacTaskManager.service.TaskService;
import com.example.RbacTaskManager.exceptions.NoContentException;
import com.example.RbacTaskManager.model.Task;
import com.example.RbacTaskManager.model.User;
import com.example.RbacTaskManager.security.CustomUserDetails;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class TaskController
{
	@Autowired
	private TaskService taskService;
	
	@GetMapping("/employee/tasks")
	public ResponseEntity<List<Task>> getAllTasks(@AuthenticationPrincipal CustomUserDetails userDetails)
	{
		User user = userDetails.getUser();
		int uId = user.getId();
		List<Task> tasks=taskService.getAllTasks(uId);
		if(tasks.isEmpty())
		{
			throw new NoContentException();
		}
		return ResponseEntity.ok(tasks);
	}
	
	@GetMapping("/employee/tasks/id/{id}")
	public ResponseEntity<Task> getTaskById(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable int id)
	{
		User user = userDetails.getUser();
		int uId = user.getId();
		return ResponseEntity.ok(taskService.getTaskById(uId,id));
	}
	
	@GetMapping("/employee/tasks/name/{name}")
	public ResponseEntity<List<Task>> getTasksByName(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable String name)
	{
		User user = userDetails.getUser();
		int uId = user.getId();
		return ResponseEntity.ok(taskService.getTasksByName(uId,name));
	}
	
	@GetMapping("/employee/tasks/highPriorityTask")
	public ResponseEntity<Task> getPriorityTask(@AuthenticationPrincipal CustomUserDetails userDetails)
	{
		User user = userDetails.getUser();
		int uId = user.getId();
		return ResponseEntity.ok(taskService.getPriorityTask(uId));
	}
	
	@GetMapping("/employee/tasks/lru")
	public ResponseEntity<List<Task>> getLruCache(@AuthenticationPrincipal CustomUserDetails userDetails)
	{
		User user = userDetails.getUser();
		int uId = user.getId();
		return ResponseEntity.ok(taskService.getLruCache(uId));
	}
	
	@GetMapping("/employee/tasks/undo")
	public ResponseEntity<Task> undoTask(@AuthenticationPrincipal CustomUserDetails userDetails)
	{
		User user = userDetails.getUser();
		int uId = user.getId();
		return ResponseEntity.ok(taskService.undoTask(uId));
	}
	
	@PostMapping("/employee/tasks/groupByTasks")
	public ResponseEntity<Map<String,List<Task>>> groupByTasks(@RequestParam(defaultValue="") String groupBy, @RequestBody List<Task> groupByTasksList)
	{
		return ResponseEntity.ok(taskService.groupTasks(groupBy,groupByTasksList));
	}
	
	@PostMapping("/employee/tasks/sortByTasks")
	public ResponseEntity<List<Task>> sortTasks(@RequestParam(defaultValue="") String sortBy, @RequestBody List<Task> sortByTasksList)
	{
		return ResponseEntity.ok(taskService.sortTasks(sortBy,sortByTasksList));
	}
	
	@PostMapping("/manager/tasks/create")
	public ResponseEntity<Task> postTask(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody Task task)
	{
		User user = userDetails.getUser();
		int uId = user.getId();
		return ResponseEntity.ok(taskService.createTask(uId,task));
	}
	
	@PutMapping("/employee/tasks/update")
	public ResponseEntity<Task> updateTask(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody Task task)
	{
		User user = userDetails.getUser();
		int uId = user.getId();
		return ResponseEntity.ok(taskService.updateTask(uId,task));
	}
	
	@DeleteMapping("/employee/tasks/delete/{id}")
	public ResponseEntity<Task> deleteTaskById(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable int id)
	{
		User user = userDetails.getUser();
		int uId = user.getId();
		return ResponseEntity.ok(taskService.deleteTask(uId,id));
	}
}
