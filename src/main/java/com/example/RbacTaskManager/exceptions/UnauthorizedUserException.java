package com.example.RbacTaskManager.exceptions;

public class UnauthorizedUserException extends RuntimeException
{
	public UnauthorizedUserException(String message)
	{
		super(message);
	}
}
