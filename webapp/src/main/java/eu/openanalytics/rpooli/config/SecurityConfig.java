/*
 * This file is part of RPooli.
 *
 * RPooli is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with RPooli.  If not, see <http://www.apache.org/licenses/>.
 */

package eu.openanalytics.rpooli.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Configuration
@ComponentScan("eu.openanalytics.rpooli")
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	
	@Autowired
	public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().withUser("guest").password("guest")
				.authorities("ROLE_GUEST");
		auth.inMemoryAuthentication().withUser("user").password("user")
				.authorities("ROLE_API_USER");
	}
	
	@Override
	public void configure(final WebSecurity web) throws Exception {
	}
	
	@Override
	protected void configure(final HttpSecurity http) throws Exception{
		http
			.authorizeRequests()
				.antMatchers(
						"/login.html*", "/logout.html*", "/openid.html*",
						"/images/**", "/css/**",
						"/js/**" )
					.permitAll()
				.anyRequest()
					.access("isAuthenticated() and hasRole('ROLE_API_USER')")
				.and()
			.logout()
				.logoutUrl("/logout")
				.logoutSuccessUrl("/logout.html")
				.invalidateHttpSession(true)
				.and()
			.httpBasic();
	}
	
}
