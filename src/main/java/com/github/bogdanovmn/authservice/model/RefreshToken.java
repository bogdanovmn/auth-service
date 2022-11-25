package com.github.bogdanovmn.authservice.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter

@Entity
@Table(name = "refresh_token")
public class RefreshToken {
	@Id
	@GeneratedValue
	private Long id;

	@OneToOne
	private Account owner;
	private String value;
	private LocalDateTime createdAt;
	private LocalDateTime expiresAt;
}
