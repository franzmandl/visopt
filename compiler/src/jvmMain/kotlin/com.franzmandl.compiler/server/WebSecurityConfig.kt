package com.franzmandl.compiler.server

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
open class WebSecurityConfig(
	@Value("\${application.security.enable-cors}") private val enableCors: Boolean,
	@Value("\${application.security.allowed-origins}") allowedOriginsString: String,
) : WebSecurityConfigurerAdapter() {
	private val allowedOrigins = allowedOriginsString.split(",").toTypedArray()

	override fun configure(http: HttpSecurity) {
		if (enableCors) {
			http.cors(Customizer.withDefaults())
		}
		http.csrf().disable()
	}

	@Bean
	open fun corsConfigurationSource(): CorsConfigurationSource {
		val source = UrlBasedCorsConfigurationSource()
		if (enableCors) {
			val configuration = CorsConfiguration()
			configuration.allowedOrigins = listOf(*allowedOrigins)
			configuration.allowedHeaders = listOf("*")
			configuration.allowedMethods = listOf("*")
			configuration.allowCredentials = true
			source.registerCorsConfiguration("/**", configuration)
		}
		return source
	}
}