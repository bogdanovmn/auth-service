package com.github.bogdanovmn.authservice.common.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "account_oauth_identity")
@ToString(onlyExplicitlyIncluded = true)
public class AccountOAuthIdentity {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
		name = "UUID",
		strategy = "org.hibernate.id.UUIDGenerator"
	)
	@ToString.Include
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id", nullable = false)
	@ToString.Include
	private Account account;

	@ToString.Include
	@Column(nullable = false, length = 32)
	private String provider;

	@ToString.Include
	@Column(name = "provider_user_id", nullable = false, length = 255)
	private String providerUserId;

	@ToString.Include
	@Column(length = 320)
	private String email;

	@Column(length = 500)
	private String name;

	@Column(insertable = false, updatable = false)
	private Date createdAt;

	@Column(insertable = false, updatable = false)
	private Date updatedAt;
}
