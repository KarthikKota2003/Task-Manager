package com.example.RbacTaskManager.service;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.RbacTaskManager.enums.Role;
import com.example.RbacTaskManager.exceptions.NoContentException;
import com.example.RbacTaskManager.exceptions.UnauthorizedUserException;
import com.example.RbacTaskManager.exceptions.UserNotFoundException;
import com.example.RbacTaskManager.exceptions.BadRequestException;
import com.example.RbacTaskManager.exceptions.ContentNotFoundException;
import com.example.RbacTaskManager.exceptions.InternalServerErrorException;
import com.example.RbacTaskManager.model.Task;
import com.example.RbacTaskManager.model.User;
import com.example.RbacTaskManager.repository.TaskRepository;

@Service
public class TaskService
{
	@Autowired
	private TaskRepository taskRepository;
	@Autowired
	private UserService userService;
	private Map<Integer,Map<Integer,Task>> userTasksMap;
	private Map<String,BiFunction<Task,Task,Integer>> sortByFunctionsMap;
	private Map<String,Function<Task,String>> groupByFunctionsMap;
	private Map<Integer,PriorityQueue<Task>> userPriorityQueueMap;
	private Map<Integer,Map<Integer,LruNode>> userLruCacheTasksMap;
	private Map<Integer,LruNode> userLruCacheHeadMap;
	private Map<Integer,LruNode> userLruCacheTailMap;
	private Map<Integer,Deque<UndoNode>> userUndoDequeMap;
	private Map<Integer,Map<Integer,Integer>> userUndoTasksMap;
	private Map<Integer,Boolean> userIsUndoOperationMap;
	
	
	private static final int CACHE_LIMIT=3;
	private static final int UNDO_LIMIT=5;
	
	public TaskService()
	{
		userTasksMap=new HashMap<>();
		userPriorityQueueMap=new HashMap<>();
		userLruCacheTasksMap=new HashMap<>();
		userLruCacheHeadMap=new HashMap<>();
		userLruCacheTailMap=new HashMap<>();
		userUndoDequeMap=new HashMap<>();
		userUndoTasksMap=new HashMap<>();
		userIsUndoOperationMap=new HashMap<>();
		loadSortByFunctions();
		loadGroupByFunctions();
	}
	
	private class LruNode
	{
		Task task;
		LruNode prev;
		LruNode next;
		public LruNode()
		{
			
		}
		public LruNode(Task task)
		{
			this.task=task;
		}
		
		public void setTask(Task task)
		{
			this.task=task;
		}
		
		public Task getTask()
		{
			return this.task;
		}
	}
	
	private class UndoNode
	{
		String name;
		Task task;
		public UndoNode(String name, Task task)
		{
			this.name=name;
			this.task=task;
		}
		public void setTask(Task task)
		{
			this.task=task;
		}
		public Task getTask()
		{
			return task;
		}
		public void setName(String name)
		{
			this.name=name;
		}
		public String getName()
		{
			return this.name;
		}
	}
	
	public void loadData(int uId)
	{
		User user=userService.getUserById(uId);
		List<Task> tasks;
		if(userTasksMap.containsKey(uId))
		{
			return;
		}
		if(user.getRole()==Role.MANAGER)
		{
			tasks=taskRepository.findByCreatedBy(user);
		}
		else
		{
			tasks=taskRepository.findByAssignedTo(user);
		}
		if(tasks.isEmpty())
		{
			userTasksMap.put(uId,new HashMap<>());
			return;
		}
		Map<Integer,Task> tasksMap=new HashMap<>();
		for(Task task:tasks)
		{
			tasksMap.put(task.getId(),task);
		}
		userTasksMap.put(uId, tasksMap);
	}
	
	public void loadSortByFunctions()
	{
		sortByFunctionsMap=new HashMap<>();
		sortByFunctionsMap.put("name",(t1,t2)->t1.getName().toLowerCase().compareTo(t2.getName().toLowerCase()));
		sortByFunctionsMap.put("priority",(t1,t2)->t1.getPriority().compareTo(t2.getPriority()));
		sortByFunctionsMap.put("status",(t1,t2)->t1.getStatus().compareTo(t2.getStatus()));
	}
	
	public void loadGroupByFunctions()
	{
		groupByFunctionsMap=new HashMap<>();
		groupByFunctionsMap.put("name",task->task.getName().toLowerCase());
		groupByFunctionsMap.put("priority",task->task.getPriority().toString());
		groupByFunctionsMap.put("status",task->task.getStatus().toString());
	}
	
	public void loadPriorityQueue(int uId)
	{
		loadData(uId);
		if(userPriorityQueueMap.containsKey(uId))
		{
			return;
		}
		PriorityQueue<Task> priorityQueue=new PriorityQueue<>((t1,t2)->t1.getPriority().compareTo(t2.getPriority()));
		Map<Integer,Task> tasksMap=userTasksMap.get(uId);
		for(Task task:tasksMap.values())
		{
			priorityQueue.offer(task);
		}
		userPriorityQueueMap.put(uId, priorityQueue);
	}
	
	public void loadLruCache(int uId)
	{
		if(userLruCacheTasksMap.containsKey(uId))
		{
			return;
		}
		LruNode head=new LruNode();
		LruNode tail=new LruNode();
		head.next=tail;
		tail.prev=head;
		userLruCacheHeadMap.put(uId, head);
		userLruCacheTailMap.put(uId,tail);
		userLruCacheTasksMap.put(uId, new HashMap<>());
	}
	
	public void loadUndo(int uId)
	{
		if(userUndoDequeMap.containsKey(uId))
		{
			return;
		}
		userUndoDequeMap.put(uId, new LinkedList<>());
		userIsUndoOperationMap.put(uId,false);
		userUndoTasksMap.put(uId,new HashMap<>());
	}
	
	public List<Task> getAllTasks(int uId)
	{
		User user=userService.getUserById(uId);
		loadData(uId);
		List<Task> tasks=new ArrayList<>();
		Map<Integer,Task> tasksMap=userTasksMap.get(uId);
		for(Task task:tasksMap.values())
		{
			tasks.add(task);
		}
//		if(tasks.isEmpty())
//		{
//			throw new NoContentException();
//		}
		return tasks;
	}
	
	public Task getTaskById(int uId, int tId)
	{
		User user=userService.getUserById(uId);
		loadData(uId);
		Map<Integer,Task> tasksMap=userTasksMap.get(uId);
		if(!tasksMap.containsKey(tId))
		{
			throw new ContentNotFoundException("No Task Found with id: "+tId);
		}
		Task task=tasksMap.get(tId);
		updateLru(uId,task);
		return task;
	}
	
	public List<Task> getTasksByName(int uId, String name)
	{
		User user=userService.getUserById(uId);
		loadData(uId);
		List<Task> tasks=new ArrayList<>();
		Map<Integer,Task> tasksMap=userTasksMap.get(uId);
		name=name.toLowerCase();
		for(Task task:tasksMap.values())
		{
			if(task.getName().toLowerCase().contains(name))
			{
				updateLru(uId,task);
				tasks.add(task);
			}
		}
		if(tasks.isEmpty())
		{
			throw new NoContentException();
		}
		return tasks;
	}
	
	public List<Task> sortTasks(String sortBy, List<Task> tasks)
	{
		if(sortBy==null || sortBy.isBlank())
		{
			throw new BadRequestException("No sort feature is selected");
		}
		String[] features=sortBy.split(",");
		for(String feature:features)
		{
			feature=feature.toLowerCase();
			if(!sortByFunctionsMap.containsKey(feature))
			{
				throw new BadRequestException("Invalid sort feature :"+feature);
			}
		}
		if(tasks.isEmpty())
		{
			throw new NoContentException();
		}
		tasks.sort((t1,t2)->{
			int result=0;
			for(String feature:features)
			{
				feature=feature.toLowerCase();
				result=sortByFunctionsMap.get(feature).apply(t1, t2);
				if(result!=0)
				{
					return result;
				}
			}
			return Integer.compare(t1.getId(), t2.getId());
		});
		return tasks;
	}
	
	public Map<String,List<Task>> groupTasks(String groupBy, List<Task> tasks)
	{
		if(groupBy==null || groupBy.isBlank())
		{
			throw new BadRequestException("No group feature is selected");
		}
		groupBy=groupBy.toLowerCase();
		if(!groupByFunctionsMap.containsKey(groupBy))
		{
			throw new BadRequestException("Invalid group feature :"+groupBy);
		}
		if(tasks.isEmpty())
		{
			throw new NoContentException();
		}
		Map<String,List<Task>> groupByTasksMap=new HashMap<>();
		Function<Task,String> function=groupByFunctionsMap.get(groupBy);
		for(Task task:tasks)
		{
			String key=function.apply(task);
			groupByTasksMap.computeIfAbsent(key, k->new ArrayList<>()).add(task);
		}
		return groupByTasksMap;
	}
	
	public Task getPriorityTask(int uId)
	{
		User user=userService.getUserById(uId);
		loadData(uId);
		loadPriorityQueue(uId);
		PriorityQueue<Task> priorityQueue=userPriorityQueueMap.get(uId);
		Map<Integer,Task> tasksMap=userTasksMap.get(uId);
		while(!priorityQueue.isEmpty())
		{
			Task task=priorityQueue.peek();
			if(!tasksMap.containsKey(task.getId()))
			{
				priorityQueue.poll();
				continue;
			}
			else
			{
				if(task==tasksMap.get(task.getId()))
				{
					updateLru(uId,task);
					return task;
				}
			}
			priorityQueue.poll();
		}
		throw new NoContentException();
	}
	
	public Task createTask(int uId, Task task)
	{
		User user=userService.getUserById(uId);
		if(task.getAssignedTo()==null)
		{
			throw new UserNotFoundException("This task is not Assigned to any valid user.");
		}
		int eId=task.getAssignedTo().getId();
		User employee=userService.getUserById(eId);
		User manager=user;
		if(manager.getRole()!=Role.MANAGER)
		{
			throw new UnauthorizedUserException("An Employee cannot create tasks.");
		}
		if(employee.getId()!=manager.getId() && employee.getRole()!=Role.EMPLOYEE)
		{
			throw new UnauthorizedUserException("A Manager cannot assign task to another Manager.");
		}
		if(employee.getId()!=manager.getId() && employee.getManager().getId()!=manager.getId())
		{
			throw new UnauthorizedUserException("A Manager cannot assign task to another Manager employees.");
		}
		loadData(manager.getId());
		loadData(employee.getId());
		task.setCreatedBy(manager);
		task.setAssignedTo(employee);
		task=taskRepository.save(task);
		Map<Integer,Task> managerTasksMap=userTasksMap.get(manager.getId());
		Map<Integer,Task> employeeTasksMap=userTasksMap.get(employee.getId());
		managerTasksMap.put(task.getId(), task);
		employeeTasksMap.put(task.getId(), task);
		loadPriorityQueue(manager.getId());
		loadPriorityQueue(employee.getId());
		PriorityQueue<Task> managerPriorityQueue=userPriorityQueueMap.get(manager.getId());
		PriorityQueue<Task> employeePriorityQueue=userPriorityQueueMap.get(employee.getId());
		managerPriorityQueue.offer(task);
		employeePriorityQueue.offer(task);
		loadUndo(uId);
		if(!userIsUndoOperationMap.get(uId))
		{
			this.addUndo(uId,"create",task);
		}
		return task;
	}
	
	public Task deleteTask(int uId, int tId)
	{
		User manager=userService.getUserById(uId);
		loadData(uId);
		Map<Integer,Task> tasksMap=userTasksMap.get(uId);
		if(!tasksMap.containsKey(tId))
		{
			throw new ContentNotFoundException("No Task Found with id: "+tId);
		}
		Task task=tasksMap.get(tId);
		int eId=task.getAssignedTo().getId();
		User employee=userService.getUserById(eId);
		if(manager.getRole()!=Role.MANAGER)
		{
			throw new UnauthorizedUserException("An Employee cannot delete tasks.");
		}
		if(uId!=task.getCreatedBy().getId())
		{
			throw new UnauthorizedUserException("This task is not owned by this Manager.");
		}
		loadData(manager.getId());
		loadData(employee.getId());
		Map<Integer,Task> managerTasksMap=userTasksMap.get(manager.getId());
		Map<Integer,Task> employeeTasksMap=userTasksMap.get(employee.getId());
		if(!managerTasksMap.containsKey(task.getId()) || !employeeTasksMap.containsKey(task.getId()))
		{
			throw new ContentNotFoundException("No Task Found with id: "+task.getId());
		}
		taskRepository.deleteById(task.getId());
		managerTasksMap.remove(task.getId());
		employeeTasksMap.remove(task.getId());
		deleteTaskLru(manager.getId(),task);
		deleteTaskLru(employee.getId(),task);
		loadUndo(manager.getId());
		loadUndo(employee.getId());
		if(!userIsUndoOperationMap.get(manager.getId()))
		{
			this.addUndo(manager.getId(),"delete",task);
		}
		this.deleteUndo(employee.getId(), task);
		return task;
	}
	
	public Task updateTask(int uId, Task newTask)
	{
		
		User user=userService.getUserById(uId);
		if(newTask.getCreatedBy()==null)
		{
			throw new UserNotFoundException("Missing owner of the Task.");
		}
		if(newTask.getAssignedTo()==null)
		{
			throw new UserNotFoundException("This task is not Assigned to any valid user.");
		}
		int tId=newTask.getId();
		int eId=newTask.getAssignedTo().getId();
		User e=userService.getUserById(eId);
		loadData(uId);
		Map<Integer,Task> tasksMap=userTasksMap.get(uId);
		if(!tasksMap.containsKey(tId))
		{
			throw new ContentNotFoundException("No Task Found with id: "+newTask.getId());
		}
		Task oldTask=tasksMap.get(tId);
		if(oldTask.getCreatedBy().getId()!=newTask.getCreatedBy().getId())
		{
			throw new UnauthorizedUserException("Users cannot change the Owner of the task.");
		}
		if(user.getRole()==Role.EMPLOYEE && (oldTask.getAssignedTo().getId()!=newTask.getAssignedTo().getId()))
		{
			throw new UnauthorizedUserException("Employee cannot assign tasks to others.");
		}
		if(user.getRole()==Role.MANAGER)
		{
			if(user.getId()!=newTask.getAssignedTo().getId() && newTask.getAssignedTo().getRole()==Role.MANAGER)
			{
				throw new UnauthorizedUserException("A Manager cannot assign task to another Manager.");
			}
		}
		if(oldTask.getAssignedTo().getId()!=newTask.getAssignedTo().getId())
		{
			User oldEmp=oldTask.getAssignedTo();
			oldEmp=userService.getUserById(oldEmp.getId());
			loadData(oldEmp.getId());
			Map<Integer,Task> oldEmpTasksMap=userTasksMap.get(oldEmp.getId());
			oldEmpTasksMap.remove(oldTask.getId());
			deleteTaskLru(oldEmp.getId(),oldTask);
			deleteUndo(oldEmp.getId(),oldTask);
		}
		User manager,employee;
		if(user.getRole()==Role.MANAGER)
		{
			manager=user;
			int employeeId=newTask.getAssignedTo().getId();
			employee=userService.getUserById(employeeId);
			if(employee.getId()!=manager.getId() && employee.getManager().getId()!=manager.getId())
			{
				throw new UnauthorizedUserException("A Manager cannot assign task to another Manager employees.");
			}
			updateTaskLru(employee.getId(),newTask);
			deleteUndo(employee.getId(),oldTask);
		}
		else
		{
			int managerId=newTask.getCreatedBy().getId();
			manager=userService.getUserById(managerId);
			employee=user;
			updateTaskLru(manager.getId(),newTask);
			deleteUndo(manager.getId(),oldTask);
		}
		newTask.setCreatedBy(manager);
		newTask.setAssignedTo(employee);
		newTask=taskRepository.save(newTask);
		loadData(manager.getId());
		loadData(employee.getId());
		Map<Integer,Task> managerTasksMap=userTasksMap.get(manager.getId());
		Map<Integer,Task> employeeTasksMap=userTasksMap.get(employee.getId());
		managerTasksMap.put(tId,newTask);
		employeeTasksMap.put(tId, newTask);
		loadPriorityQueue(manager.getId());
		loadPriorityQueue(employee.getId());
		PriorityQueue<Task> managerPriorityQueue=userPriorityQueueMap.get(manager.getId());
		PriorityQueue<Task> employeePriorityQueue=userPriorityQueueMap.get(employee.getId());
		managerPriorityQueue.offer(newTask);
		employeePriorityQueue.offer(newTask);
		updateLru(user.getId(),newTask);
		if(!userIsUndoOperationMap.get(user.getId()))
		{
			addUndo(user.getId(),"update",oldTask);
		}
		return newTask;
	}
	
	public void deleteTaskLru(int uId, Task task)
	{
		loadLruCache(uId);
		Map<Integer,LruNode> cacheTasksMap=userLruCacheTasksMap.get(uId);
		if(!cacheTasksMap.containsKey(task.getId()))
		{
			return;
		}
		LruNode node=cacheTasksMap.get(task.getId());
		LruNode next=node.next;
		LruNode prev=node.prev;
		next.prev=prev;
		prev.next=next;
		cacheTasksMap.remove(task.getId());
	}
	
	public void updateLru(int uId, Task task)
	{
		loadLruCache(uId);
		Map<Integer,LruNode> cacheTasksMap=userLruCacheTasksMap.get(uId);
		LruNode head=userLruCacheHeadMap.get(uId);
		LruNode tail=userLruCacheTailMap.get(uId);
		deleteTaskLru(uId,task);
		LruNode newNode=new LruNode(task);
		cacheTasksMap.put(task.getId(), newNode);
		LruNode first=head.next;
		newNode.prev=head;
		newNode.next=first;
		head.next=newNode;
		first.prev=newNode;
		if(cacheTasksMap.size()>CACHE_LIMIT)
		{
			deleteTaskLru(uId,tail.prev.getTask());
		}
	}
	
	public void updateTaskLru(int uId, Task task)
	{
		loadLruCache(uId);
		Map<Integer,LruNode> cacheTasksMap=userLruCacheTasksMap.get(uId);
		if(!cacheTasksMap.containsKey(task.getId()))
		{
			return;
		}
		LruNode node=cacheTasksMap.get(task.getId());
		node.setTask(task);
	}
	
	public List<Task> getLruCache(int uId)
	{
		User user=userService.getUserById(uId);
		loadData(uId);
		loadLruCache(uId);
		List<Task> tasks=new ArrayList<>();
		LruNode head=userLruCacheHeadMap.get(uId);
		LruNode tail=userLruCacheTailMap.get(uId);
		LruNode current=head.next;
		while(current!=tail)
		{
			Task task=current.getTask();
			tasks.add(task);
			current=current.next;
		}
		if(tasks.isEmpty())
		{
			throw new NoContentException();
		}
		return tasks;
	}
	
	public void deleteUndo(int uId, Task task)
	{
		loadUndo(uId);
		Map<Integer,Integer> oldUndoTasksMap=userUndoTasksMap.get(uId);
		Map<Integer,Integer> newUndoTasksMap=new HashMap<>();
		if(!oldUndoTasksMap.containsKey(task.getId()))
		{
			return;
		}
		Deque<UndoNode> oldUndo=userUndoDequeMap.get(uId);
		Deque<UndoNode> newUndo=new LinkedList<>();
		while(!oldUndo.isEmpty())
		{
			UndoNode node=oldUndo.removeFirst();
			if(node.getTask().getId()==task.getId())
			{
				break;
			}
			newUndo.addLast(node);
			newUndoTasksMap.put(node.getTask().getId(),newUndoTasksMap.getOrDefault(node.getTask().getId(),0)+1);
		}
		userUndoDequeMap.put(uId, newUndo);
		userUndoTasksMap.put(uId, newUndoTasksMap);
	}
	
	public void addUndo(int uId, String name, Task task)
	{
		loadUndo(uId);
		UndoNode node=new UndoNode(name,task);
		Map<Integer,Integer> undoMap=userUndoTasksMap.get(uId);
		undoMap.put(task.getId(),undoMap.getOrDefault(task.getId(),0)+1);
		Deque<UndoNode> undoDeque=userUndoDequeMap.get(uId);
		undoDeque.addFirst(node);
		if(undoDeque.size()>UNDO_LIMIT)
		{
			UndoNode d=undoDeque.removeLast();
			Task t=d.getTask();
			undoMap.put(t.getId(),undoMap.get(t.getId())-1);
			if(undoMap.get(t.getId())==0)
			{
				undoMap.remove(t.getId());
			}
		}
	}
	
	public Task undoTask(int uId)
	{
		User user=userService.getUserById(uId);
		loadUndo(uId);
		Deque<UndoNode> undoDeque=userUndoDequeMap.get(uId);
		if(undoDeque.isEmpty())
		{
			throw new NoContentException();
		}
		UndoNode last=undoDeque.removeFirst();
		Task task=last.getTask();
		Map<Integer,Integer> undoMap=userUndoTasksMap.get(uId);
		undoMap.put(task.getId(),undoMap.getOrDefault(task.getId(),1)-1);
		if(undoMap.get(task.getId())==0)
		{
			undoMap.remove(task.getId());
		}
		userIsUndoOperationMap.put(uId,true);
		Task responseTask=null;
		try
		{
			if("create".equals(last.getName()))
				{
					responseTask=deleteTask(uId,last.getTask().getId());
				}
				else if("delete".equals(last.getName()))
				{
					Task removedTask=last.getTask();
					removedTask.setId(0);
					responseTask=createTask(uId,removedTask);
				}
				else if("update".equals(last.getName()))
				{
					responseTask=updateTask(uId,last.getTask());
				}
				else
				{
					throw new InternalServerErrorException("Invalid Undo feature found: "+last.getName());
				}
		}
		finally
		{
		userIsUndoOperationMap.put(uId,false);
		}
		return responseTask;
	}
}
