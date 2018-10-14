package com.in28minutes.springboot.rest.example.student;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="Not Found")
public class NotFoundException extends RuntimeException {

	public NotFoundException(String screenName) {
		super(screenName + " Not Found");
	}

}
