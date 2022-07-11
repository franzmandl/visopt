package com.franzmandl.compiler.server

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebMvc
open class MvcConfig(
	@Value("\${application.path.web:}") val webPath: String,
) : WebMvcConfigurer {
	override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
		if (webPath.isNotEmpty()) {
			registry
				.addResourceHandler("/**")
				.addResourceLocations("file:$webPath/")
		}
	}

	override fun addViewControllers(registry: ViewControllerRegistry) {
		if (webPath.isNotEmpty()) {
			registry.addRedirectViewController("/", "index.html")
		}
	}
}