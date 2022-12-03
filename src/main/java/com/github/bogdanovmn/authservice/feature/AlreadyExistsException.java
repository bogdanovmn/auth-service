package com.github.bogdanovmn.authservice.feature;

public class AlreadyExistsException extends RuntimeException {
	public AlreadyExistsException(String id) {
		super("Entity with id = '%s' is already exists".formatted(id));
	}
}
