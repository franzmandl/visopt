package com.franzmandl.compiler.server

import com.franzmandl.compiler.Compiler
import com.franzmandl.compiler.generated.GitInfo
import com.franzmandl.compiler.misc.PhaseException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping(value = ["/compiler"])
class CompilerResource {
	private val logger = LoggerFactory.getLogger(CompilerResource::class.java)
	private val compilerVersion = "Compiler version: branch=${GitInfo.branch}, shortHash=${GitInfo.shortHash}, tags=[${GitInfo.tags.joinToString(", ")}]"

	init {
		logger.info(compilerVersion)
	}

	@RequestMapping(value = ["/clientError"], method = [RequestMethod.POST])
	@ResponseBody
	fun postClientError(
		@RequestBody(required = false) requestBody: String?,
	) {
		logger.error("A client error occurred!\n$compilerVersion\nClient report: ${requestBody.orEmpty()}")
	}

	@RequestMapping(value = ["/clientWarning"], method = [RequestMethod.POST])
	@ResponseBody
	fun postClientWarning(
		@RequestBody(required = false) requestBody: String?,
	) {
		logger.warn("A client warning occurred!\n$compilerVersion\nClient report: ${requestBody.orEmpty()}")
	}

	@RequestMapping(value = ["/typeChecker/jova"], method = [RequestMethod.POST])
	@ResponseBody
	fun postTypeCheckerJova(
		@RequestParam(defaultValue = "noSource") fileName: String,
		@RequestBody requestBody: String,
	): ResponseEntity<String> {
		val responseHeaders = HttpHeaders()
		responseHeaders.contentType = MediaType.APPLICATION_JSON
		val responseEntity = try {
			val responseBody = Compiler.fromString(fileName, requestBody).checkTypes().toJsonString()
			ResponseEntity(responseBody, responseHeaders, HttpStatus.OK)
		} catch (e: PhaseException) {
			ResponseEntity(e.messages.toJsonString(), responseHeaders, HttpStatus.BAD_REQUEST)
		} catch (e: Throwable) {
			throw Exception(requestBody, e)
		}
		return responseEntity
	}
}