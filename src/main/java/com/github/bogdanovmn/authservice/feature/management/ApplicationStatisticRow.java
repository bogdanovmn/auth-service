package com.github.bogdanovmn.authservice.feature.management;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class ApplicationStatisticRow {
	long appId;
	String appName;
	String shortDescription;
	String url;
	Boolean active;
	String roleName;
	int usersCount;
}
