package com.example.RbacTaskManager.exceptions;

public class BadRequestException extends RuntimeException
{
	public BadRequestException(String message)
	{
		super(message);
	}
}
