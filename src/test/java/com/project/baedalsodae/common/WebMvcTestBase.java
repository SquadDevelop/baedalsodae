package com.project.baedalsodae.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

public class WebMvcTestBase {
	@Autowired
	protected MockMvc mockMvc;

	@Autowired protected ObjectMapper objectMapper;

	protected UUID userId;

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
	}
}
