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

import it.smartcommunitylab.carpooling.model.Community;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

@Component
public class UserCommunitiesSetup {

	@Value("classpath:/usercommunities.yml")
	private Resource resource;

	@PostConstruct
	public void init() throws IOException {
		Yaml yaml = new Yaml(new Constructor(UserCommunitiesSetup.class));
		UserCommunitiesSetup data = (UserCommunitiesSetup) yaml.load(resource.getInputStream());
		this.userCommunities = data.userCommunities;
	}

	private List<Community> userCommunities;

	public List<Community> getUserCommunities() {
		return userCommunities;
	}

	public void setUserCommunities(List<Community> userCommunities) {
		this.userCommunities = userCommunities;
	}

}
