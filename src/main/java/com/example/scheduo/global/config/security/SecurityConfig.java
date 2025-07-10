package com.example.scheduo.global.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.example.scheduo.global.auth.CustomOAuth2UserService;
import com.example.scheduo.global.auth.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.scheduo.global.config.security.entry.CustomAuthenticationEntryPoint;
import com.example.scheduo.global.config.security.filter.JwtFilter;
import com.example.scheduo.global.config.security.handler.CustomOAuth2FailureHandler;
import com.example.scheduo.global.config.security.handler.CustomOAuth2SuccessHandler;
import com.example.scheduo.global.config.security.provider.JwtProvider;
import com.example.scheduo.global.utils.AuthExcludedUris;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final CustomOAuth2UserService customOAuth2UserService;
	private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
	private final CustomOAuth2FailureHandler customOAuth2FailureHandler;
	private final JwtProvider jwtProvider;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.cors(cors -> cors.configurationSource(new CorsConfigurationSource() {
				@Override
				public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
					CorsConfiguration config = new CorsConfiguration();
					config.addAllowedOrigin("http://localhost:3000");
					config.addAllowedOrigin("https://scheduo.store");
					config.addAllowedMethod("*");
					config.addAllowedHeader("*");
					config.setAllowCredentials(true);
					return config;
				}
			}))
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement((session) -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(request -> request
				.requestMatchers(AuthExcludedUris.ALL).permitAll()
				.anyRequest().authenticated())
			.oauth2Login(oauth2 -> oauth2
				.authorizationEndpoint(authorization -> authorization
					.authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository))
				.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
				.successHandler(customOAuth2SuccessHandler)
				.failureHandler(customOAuth2FailureHandler))
			.addFilterBefore(new JwtFilter(jwtProvider, new AntPathMatcher()),
				UsernamePasswordAuthenticationFilter.class)
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint(customAuthenticationEntryPoint)
			);

		return http.build();
	}
}
