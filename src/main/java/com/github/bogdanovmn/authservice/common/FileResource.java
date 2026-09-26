package com.github.bogdanovmn.authservice.common;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@RequiredArgsConstructor
public class FileResource {
	public static final String CLASSPATH_PREFIX = "classpath:";

	@NonNull
	private final String fileName;

	public byte[] content() throws IOException {
		return fileName.startsWith(CLASSPATH_PREFIX)
			? internalFileContent()
			: externalFileContent();
	}

	public String contentAsString() throws IOException {
		return new String(content(), StandardCharsets.UTF_8);
	}

	private byte[] externalFileContent() throws IOException {
		return Files.readAllBytes(Paths.get(fileName));
	}

	private byte[] internalFileContent() throws IOException {
		String path = fileName.replaceFirst(CLASSPATH_PREFIX, "");

		// ClassLoader.getSystemResourceAsStream() doesn't see resources packaged
		// into an executable Spring Boot jar (they live in BOOT-INF/classes), so
		// the class's own classloader is used, with a context classloader fallback
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader == null) {
			classLoader = FileResource.class.getClassLoader();
		}

		try (
			InputStream file = classLoader.getResourceAsStream(path)
		) {
			if (file == null) {
				throw new FileNotFoundException(
					"Resource '%s' has not been found in the classpath".formatted(path)
				);
			}
			return file.readAllBytes();
		}
	}
}
