package com.github.bogdanovmn.authservice.common.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Setter

@Entity
@Table(name = "account_security_event")
@ToString(onlyExplicitlyIncluded = true)
public class AccountSecurityEvent {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ToString.Include
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id")
	@ToString.Include
	private Account account;

	@Enumerated(EnumType.STRING)
	@ToString.Include
	private AccountSecurityEventType type;

	@ToString.Include
	private String ip;

	@Column(name = "user_agent")
	@ToString.Include
	private String userAgent;

	@Column(insertable = false, updatable = false)
	private Date createdAt;
}