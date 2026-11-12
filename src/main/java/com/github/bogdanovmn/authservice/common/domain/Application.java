package com.github.bogdanovmn.authservice.common.domain;

import lombok.Builder;
import lombok.Value;

import java.util.Date;

@Value
@Builder
public class Application {
	public static final String ANY_APPLICATION = "any";

	Long id;
	String name;
	String shortDescription;
	String url;

	@Builder.Default
	Boolean active = true;

	Date createdAt;
}
