package com.pritam.techmate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

	    http
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/", "/css/**", "/js/**", "/images/**").permitAll()
	            .anyRequest().authenticated()
	        )
	        .oauth2Login(oauth -> oauth
	            .defaultSuccessUrl("/dashboard", true)
	        )
	        .logout(logout -> logout
	            .logoutSuccessUrl("/")
	        );

	    return http.build();
	}
}
