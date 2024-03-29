package com.github.bogdanovmn.authservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

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
@ToString(onlyExplicitlyIncluded = true)
public class RefreshToken {
	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
		name = "UUID",
		strategy = "org.hibernate.id.UUIDGenerator"
	)
	@ToString.Include
	private UUID id;

	@OneToOne
	@JoinColumn(name = "account_id")
	@ToString.Include
	private Account account;

	@Column(insertable = false, updatable = false)
	private Date createdAt;

	@ToString.Include
	private Date expiresAt;
}
