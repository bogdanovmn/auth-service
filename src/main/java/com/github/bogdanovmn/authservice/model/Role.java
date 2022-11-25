package com.github.bogdanovmn.authservice.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter

@Entity
@Table(name = "role")
public class Role {
	public enum Name { user, admin, moderator }

	@Id
	@GeneratedValue
	private Long id;

	@Enumerated(EnumType.STRING)
	private Name name;

	@ManyToOne
	@JoinColumn(name = "app_id")
	private Application application;

	@Override
	public String toString() {
		return "%s:%s".formatted(
			getApplication().getName(),
			getName()
		);
	}
}
