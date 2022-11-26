package com.github.bogdanovmn.authservice.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter

@Entity
@Table(name = "refresh_token")
public class RefreshToken {
	@Id
	@GeneratedValue
	private UUID id;

	@OneToOne
	@JoinColumn(name = "account_id")
	private Account account;

	@Column(insertable = false, updatable = false)
	private Date createdAt;

	private Date expiresAt;
}
