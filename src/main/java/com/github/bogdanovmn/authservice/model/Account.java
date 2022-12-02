package com.github.bogdanovmn.authservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter

@Entity
@Table(name = "account")
@ToString(onlyExplicitlyIncluded = true)
public class Account {
	@Id
	@GeneratedValue
	@ToString.Include
	private UUID id;

	private String name;
	private String encodedPassword;
	@ToString.Include
	private String email;

	@ToString.Include
	private boolean enabled;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
		name = "account2role",
		joinColumns = @JoinColumn(name = "account_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
	)
	private Set<Role> roles;

	@Column(insertable = false, updatable = false)
	private Date createdAt;

	@Column(insertable = false, updatable = false)
	private Date updatedAt;
}
