package com.github.bogdanovmn.authservice.common.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Setter

@Entity
@Table(name = "failed_login_attempt")
@ToString(onlyExplicitlyIncluded = true)
public class FailedLoginAttempt {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ToString.Include
	private Long id;

	@ToString.Include
	private String email;

	@ToString.Include
	private String ip;

	@Column(name = "user_agent")
	@ToString.Include
	private String userAgent;

	@Column(insertable = false, updatable = false)
	private Date createdAt;
}