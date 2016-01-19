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
package it.smartcommunitylab.carpooling.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class CustomAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	//	@Autowired
	//	private ProviderSetup appSetup;

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
	}

	@Override
	protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		// bypass check for reporter role: authentication has already been done
		if (CarPoolingUserDetails.isCarPooler(authentication)) {
			return new CarPoolingUserDetails(username);
		}
		//		ProviderSettings app = appSetup.findProviderById(username);
		//		if (app == null) {
		//			throw new UsernameNotFoundException(username);
		//		}
		//		if (!app.getPassword().equals(authentication.getCredentials().toString())) {
		//			throw new BadCredentialsException("Incorrect password");
		//		}
		return null;
	}

	//	public class AppDetails implements UserDetails {
	//		private static final long serialVersionUID = 1970015369860723085L;
	//
	//		private ProviderSettings app;
	//		
	//		public AppDetails() {
	//			super();
	//		}
	//
	//		public AppDetails(ProviderSettings app) {
	//			super();
	//			this.app = app;
	//		}
	//
	//
	//		@Override
	//		public Collection<? extends GrantedAuthority> getAuthorities() {
	//			return Collections.singletonList(new SimpleGrantedAuthority(app.getId()));
	//		}
	//
	//		@Override
	//		public String getPassword() {
	//			return app.getPassword();
	//		}
	//
	//		@Override
	//		public String getUsername() {
	//			return app.getId();
	//		}
	//
	//		@Override
	//		public boolean isAccountNonExpired() {
	//			return true;
	//		}
	//
	//		@Override
	//		public boolean isAccountNonLocked() {
	//			return true;
	//		}
	//
	//		@Override
	//		public boolean isCredentialsNonExpired() {
	//			return true;
	//		}
	//
	//		@Override
	//		public boolean isEnabled() {
	//			return true;
	//		}
	//
	//		public ProviderSettings getApp() {
	//			return app;
	//		}
	//
	//		public void setApp(ProviderSettings app) {
	//			this.app = app;
	//		}
	//	}

}
