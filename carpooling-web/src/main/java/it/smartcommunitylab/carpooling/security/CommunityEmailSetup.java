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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

@Component
public class CommunityEmailSetup {

	@Value("classpath:/emails.yml")
	private Resource resource;

	@PostConstruct
	public void init() throws IOException {
		Yaml yaml = new Yaml(new Constructor(CommunityEmailSetup.class));
		CommunityEmailSetup data = (CommunityEmailSetup) yaml.load(resource.getInputStream());
		this.communityWithEmails = data.communityWithEmails;
	}

	private List<CommunityWithEmails> communityWithEmails;

	public List<String> getEmailAccounts(String id) {
		// get community Id email list.
		List<String> emails = new ArrayList<String>();
		for (CommunityWithEmails community : communityWithEmails)
			if (community.getId().equalsIgnoreCase(id)) {
				emails.addAll(community.getEmails());

			}
		return emails;
	}

	public List<CommunityWithEmails> getCommunityWithEmails() {
		return communityWithEmails;
	}

	public void setCommunityWithEmails(List<CommunityWithEmails> communityWithEmails) {
		this.communityWithEmails = communityWithEmails;
	}

}

class CommunityWithEmails {
	public String id;
	public List<String> emails;

	public CommunityWithEmails() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getEmails() {
		return emails;
	}

	public void setEmails(List<String> emails) {
		this.emails = emails;
	}

}
