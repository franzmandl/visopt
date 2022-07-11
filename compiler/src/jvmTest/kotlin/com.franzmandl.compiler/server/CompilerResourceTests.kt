package com.franzmandl.compiler.server

import com.franzmandl.compiler.suite.Constant
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.nio.file.Files
import kotlin.io.path.Path

@SpringBootTest
class CompilerResourceTests(
	@Autowired val compilerResource: CompilerResource,
) {
	lateinit var mockMvc: MockMvc

	@BeforeEach
	fun setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(compilerResource).build()
	}

	@Test
	fun parserFail() {
		val result = mockMvc.perform(post("/compiler/typeChecker/jova?fileName=in.jova").content(Files.readString(Path("${Constant.testCasesDirectory}/parser/public/fail01/in.jova"))))
			.andExpect(status().isBadRequest)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andReturn().response.contentAsString
		Assertions.assertThat(result).isNotEmpty
	}

	@Test
	fun typeCheckerWarning() {
		val result =
			mockMvc.perform(post("/compiler/typeChecker/jova?fileName=in.jova").content(Files.readString(Path("${Constant.testCasesDirectory}/type_checker/public/coercion_warning/warning02/in.jova"))))
				.andExpect(status().isOk)
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().response.contentAsString
		Assertions.assertThat(result).isNotEmpty
	}

	@Test
	fun emptyClientError() {
		val result = mockMvc.perform(post("/compiler/clientError"))
			.andExpect(status().isOk)
			.andReturn().response.contentAsString
		Assertions.assertThat(result).isEmpty()
	}

	@Test
	fun clientError() {
		val result = mockMvc.perform(post("/compiler/clientError").content("Content"))
			.andExpect(status().isOk)
			.andReturn().response.contentAsString
		Assertions.assertThat(result).isEmpty()
	}

	@Test
	fun emptyClientWarning() {
		val result = mockMvc.perform(post("/compiler/clientWarning"))
			.andExpect(status().isOk)
			.andReturn().response.contentAsString
		Assertions.assertThat(result).isEmpty()
	}

	@Test
	fun clientWarning() {
		val result = mockMvc.perform(post("/compiler/clientWarning").content("Content"))
			.andExpect(status().isOk)
			.andReturn().response.contentAsString
		Assertions.assertThat(result).isEmpty()
	}
}