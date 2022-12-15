package com.github.bogdanovmn.authservice.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@Table(name = "role")
public class Role {
	public enum Name { user, admin, moderator }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private Name name;

	@ManyToOne
	@JoinColumn(name = "app_id")
	private Application application;

	@Column(insertable = false, updatable = false)
	private Date createdAt;

	@Override
	public String toString() {
		return "%s:%s".formatted(
			getApplication().getName(),
			getName()
		);
	}
}
