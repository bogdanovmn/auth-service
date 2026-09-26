package com.github.bogdanovmn.authservice.common;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileResourceTest {

	@Test
	void classpathResourceIsReadable() throws Exception {
		byte[] expected = Files.readAllBytes(Path.of("src/test/resources/jwt/public.der"));

		assertArrayEquals(
			expected,
			new FileResource(FileResource.CLASSPATH_PREFIX + "jwt/public.der").content()
		);
	}

	@Test
	void classpathTextResourceIsReadable() throws Exception {
		String content = new FileResource(
			FileResource.CLASSPATH_PREFIX + "migration/db/changelog.xml"
		).contentAsString();

		assertTrue(content.contains("account_status_domain"));
	}

	@Test
	void missingClasspathResourceIsReportedClearly() {
		FileNotFoundException error = assertThrows(
			FileNotFoundException.class,
			() -> new FileResource(FileResource.CLASSPATH_PREFIX + "jwt/nope.der").content()
		);

		assertEquals(
			"Resource 'jwt/nope.der' has not been found in the classpath",
			error.getMessage()
		);
	}

	@Test
	void missingExternalFileIsReported() {
		assertThrows(
			java.io.IOException.class,
			() -> new FileResource("no-such-dir/no-such-file.der").content()
		);
	}

	@Test
	void externalFileIsReadable() throws Exception {
		Path file = Files.createTempFile("file-resource", ".txt");
		try {
			Files.write(file, "content".getBytes(StandardCharsets.UTF_8));

			assertEquals(
				"content",
				new FileResource(file.toString()).contentAsString()
			);
		} finally {
			Files.deleteIfExists(file);
		}
	}
}
