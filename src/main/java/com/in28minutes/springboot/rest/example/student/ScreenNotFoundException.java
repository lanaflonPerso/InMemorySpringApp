package com.in28minutes.springboot.rest.example.student;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="Screen Not Found")
public class ScreenNotFoundException extends RuntimeException {

	public ScreenNotFoundException(String screenName) {
		super(screenName + " Not Found");
	}

}
