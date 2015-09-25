/**
 * Copyright 2015 Smart Community Lab
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.smartcommunitylab.carpooling.security;

import it.smartcommunitylab.carpooling.managers.UserManager;
import it.smartcommunitylab.carpooling.model.User;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.filter.GenericFilterBean;

import eu.trentorise.smartcampus.profileservice.BasicProfileService;
import eu.trentorise.smartcampus.profileservice.model.BasicProfile;

/**
 * 
 * @author nawazk
 *
 */
public class OAuthFilter extends GenericFilterBean {
	private static final String BEARER_TYPE = "bearer ";
	private static final String AUTHORIZATION = "Authorization";

	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private RememberMeServices rememberMeServices;
	@Autowired
	private Environment env;
	@Autowired
	private UserManager userManager;

	private BasicProfileService profileService;

	@PostConstruct
	private void init() {
		profileService = new BasicProfileService(env.getProperty("ext.aacURL"));
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		if (request instanceof HttpServletRequest && isAuthenticationRequired()) {
			try {

				String authToken = extractToken((HttpServletRequest) request);
				BasicProfile basicProfile = profileService.getBasicProfile(authToken);
				UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
						basicProfile.getUserId(), basicProfile.getUserId(), CarPoolingUserDetails.CARPOOLER_AUTHORITIES);
				token.setDetails(new WebAuthenticationDetails((HttpServletRequest) request));
				Authentication authenticatedUser = authenticationManager.authenticate(token);
				SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
				User user = User.fromUserProfile(basicProfile);
				userManager.saveUser(user);
				SecurityContextHolder.getContext().setAuthentication(token);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		chain.doFilter(request, response);
	}

	private boolean isAuthenticationRequired() {
		// apparently filters have to check this themselves.  So make sure they have a proper AuthenticatedAccount in their session.
		Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
		if ((existingAuth == null) || !existingAuth.isAuthenticated()) {
			return true;
		}

		return false;
	}

	private String extractToken(HttpServletRequest request) {
		String completeToken = request.getHeader(AUTHORIZATION);
		if (completeToken == null)
			return null;
		if (completeToken.toLowerCase().startsWith(BEARER_TYPE)) {
			completeToken = completeToken.substring(BEARER_TYPE.length());
		}
		return completeToken;
	}

}