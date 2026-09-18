package com.github.bogdanovmn.authservice.infrastructure.web;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Component
@Scope(
	value = WebApplicationContext.SCOPE_REQUEST,
	proxyMode = ScopedProxyMode.TARGET_CLASS
)
public class ClientInfo {

	public String ip() {
		String forwarded = currentRequest().getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		String realIp = currentRequest().getHeader("X-Real-IP");
		if (realIp != null && !realIp.isBlank()) {
			return realIp.trim();
		}
		return currentRequest().getRemoteAddr();
	}

	public String userAgent() {
		return currentRequest().getHeader("User-Agent");
	}

	private HttpServletRequest currentRequest() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	}
}