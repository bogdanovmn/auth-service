package com.github.bogdanovmn.authservice.infrastructure.config;

import com.github.bogdanovmn.authservice.AlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandling {

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<ExceptionResponse> defaultError(HttpServletRequest req, Exception ex) throws Exception {
		if (AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class) != null) {
			throw ex;
		}

		return exceptionResponse(req, ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
	}

	@ExceptionHandler(value = {
		JpaObjectRetrievalFailureException.class,
		NoSuchElementException.class
	})
	public ResponseEntity<ExceptionResponse> notFoundException(HttpServletRequest req, Exception ex) throws Exception {
		return exceptionResponse(req, ex, HttpStatus.NOT_FOUND.value());
	}

	@ExceptionHandler(value = AlreadyExistsException.class)
	public ResponseEntity<ExceptionResponse> alreadyExistsException(HttpServletRequest req, Exception ex) throws Exception {
		return exceptionResponse(req, ex, HttpStatus.CONFLICT.value());
	}

	@ExceptionHandler(value = {
		BindException.class,
		IllegalArgumentException.class
	})
	public ResponseEntity<ExceptionResponse> badRequest(HttpServletRequest req, Exception ex) throws Exception {
		return exceptionResponse(req, ex, HttpStatus.BAD_REQUEST.value());
	}

	private ResponseEntity<ExceptionResponse> exceptionResponse(HttpServletRequest req, Throwable ex, int statusCode) {
		boolean isServerError = statusCode >= 500;
		if (isServerError) {
			log.error(
				"HTTP Response: {} for [{} {}] processing error: {}",
				statusCode, req.getMethod(), req.getRequestURI(), exceptionMessage(ex), ex
			);
		} else {
			log.warn(
				"HTTP Response: {} for [{} {}] processing error: {}",
				statusCode, req.getMethod(), req.getRequestURI(), exceptionMessage(ex)
			);
		}
		return ResponseEntity.status(statusCode).body(
			ExceptionResponse.builder()
				.message(ex.getMessage())
				.code(statusCode)
				.exception(ex.getClass().getName())
				.stacktrace(
					isServerError
						? Arrays.stream(
							ex.getStackTrace()
						).map(StackTraceElement::toString)
							.limit(10)
							.collect(Collectors.toList())
						: null
				)
				.build()
		);
	}

	private String exceptionMessage(Throwable exception) {
		return Optional.ofNullable(exception.getMessage())
			.orElse(
				Optional.ofNullable(exception.getCause())
					.map(Throwable::getMessage)
					.orElse(exception.getClass().getSimpleName())
			);

	}
}