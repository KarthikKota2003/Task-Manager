package com.example.RbacTaskManager.controllerAdvice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.RbacTaskManager.exceptions.BadRequestException;
import com.example.RbacTaskManager.exceptions.ContentNotFoundException;
import com.example.RbacTaskManager.exceptions.InternalServerErrorException;
import com.example.RbacTaskManager.exceptions.NoContentException;
import com.example.RbacTaskManager.exceptions.UnauthorizedUserException;
import com.example.RbacTaskManager.exceptions.UserNotFoundException;

@RestControllerAdvice
public class HandleGlobalExceptions
{
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException e)
	{
		return ResponseEntity.status(404).body(e.getMessage());
	}
	
	@ExceptionHandler(UnauthorizedUserException.class)
	public ResponseEntity<String> handleUnauthorizedUserException(UnauthorizedUserException e)
	{
		return ResponseEntity.status(403).body(e.getMessage());
	}
	
	@ExceptionHandler(NoContentException.class)
	public ResponseEntity<String> handleNoContentException(NoContentException e)
	{
		return ResponseEntity.status(204).build();
	}
	
	@ExceptionHandler(ContentNotFoundException.class)
	public ResponseEntity<String> handleContentNotFoundException(ContentNotFoundException e)
	{
		return ResponseEntity.status(404).body(e.getMessage());
	}
	
	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<String> handleBadRequestException(BadRequestException e)
	{
		return ResponseEntity.status(400).body(e.getMessage());
	}
	
	@ExceptionHandler(InternalServerErrorException.class)
	public ResponseEntity<String> handleBadRequestException(InternalServerErrorException e)
	{
		return ResponseEntity.status(500).body(e.getMessage());
	}
}
