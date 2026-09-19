package com.github.bogdanovmn.authservice.feature.management;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class UpdateAppRequest {
	String name;
	String shortDescription;
	String url;
}