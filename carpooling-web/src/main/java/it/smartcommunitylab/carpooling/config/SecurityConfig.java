/*******************************************************************************
 * Copyright 2015 Smart Community Lab
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package it.smartcommunitylab.carpooling.config;

import it.smartcommunitylab.carpooling.security.CarPoolingUserDetails;
import it.smartcommunitylab.carpooling.security.OAuthFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebMvcSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter{

	@Autowired
	private UserDetailsService userDetailsServiceImpl;
	
	@Autowired
	private AuthenticationProvider consoleAuthenticationProvider;
	
	@Autowired
	private Environment env;

	@Autowired
	@Value("${rememberme.key}")
	private String rememberMeKey;
		
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
	    auth
	    .authenticationProvider(consoleAuthenticationProvider)
	    .authenticationProvider(rememberMeAuthenticationProvider());
	}
	@Override
	protected void configure(HttpSecurity http) throws Exception {
	        http
        	.csrf()
        		.disable();
	        
	        http
	        .rememberMe();
	        
	        http
	        .authorizeRequests()
	        	.antMatchers("/api/**")
	        		.hasAnyAuthority(CarPoolingUserDetails.CARPOOLER).and()
	        .addFilterBefore(rememberMeAuthenticationFilter(), BasicAuthenticationFilter.class)
	        .addFilterBefore(oauthAuthenticationFilter(), BasicAuthenticationFilter.class);

	        http
            .authorizeRequests()
                .anyRequest()
                	.permitAll();
	        http
            .formLogin()
                .loginPage("/login")
                	.permitAll()
                	.and()
                .logout()
                	.permitAll().deleteCookies("rememberme","JSESSIONID");
	 }

	@Bean 
	public OAuthFilter oauthAuthenticationFilter() throws Exception{
		 return new OAuthFilter();
	}

	@Bean 
	public RememberMeAuthenticationFilter rememberMeAuthenticationFilter() throws Exception{
		 return new RememberMeAuthenticationFilter(authenticationManager(), tokenBasedRememberMeService());
	}
//	@Bean 
	public RememberMeAuthenticationProvider rememberMeAuthenticationProvider(){
		 return new RememberMeAuthenticationProvider(tokenBasedRememberMeService().getKey());
	}
	@Bean 
	public TokenBasedRememberMeServices tokenBasedRememberMeService(){
		 TokenBasedRememberMeServices service = new TokenBasedRememberMeServices(env.getProperty("rememberme.key"), userDetailsServiceImpl);
		 service.setAlwaysRemember(true);
		 service.setCookieName("rememberme");
		 service.setTokenValiditySeconds(3600*24*365*100);
		 return service;
	}
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
}
