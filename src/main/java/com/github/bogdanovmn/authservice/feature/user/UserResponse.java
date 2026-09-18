package com.github.bogdanovmn.authservice.feature.user;

import lombok.Builder;
import lombok.Value;

import java.util.Date;
import java.util.UUID;

@Value
@Builder
class UserResponse {
	UUID id;
	String name;
	String email;
	String status;
	Date createdAt;
	Date updatedAt;
}