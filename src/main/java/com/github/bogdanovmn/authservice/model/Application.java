package com.github.bogdanovmn.authservice.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Getter
@Setter

@Entity
@Table(name = "application")
public class Application {
	public static final String ANY_APPLICATION = "any";

	@Id
	@GeneratedValue
	private Long id;

	private String name;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "application")
	private List<Role> roles;

	@Column(insertable = false, updatable = false)
	private Date createdAt;
}
