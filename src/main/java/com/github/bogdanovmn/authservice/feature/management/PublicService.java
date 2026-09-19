package com.github.bogdanovmn.authservice.feature.management;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class PublicService {
	String name;
	String shortDescription;
	String url;
}