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

package it.smartcommunitylab.carpooling.mongo.repos.impl;

import it.smartcommunitylab.carpooling.model.Community;
import it.smartcommunitylab.carpooling.mongo.repos.CommunityRepoCustom;
import it.smartcommunitylab.carpooling.mongo.repos.CommunityRepository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class CommunityRepositoryImpl implements CommunityRepoCustom {

	@Autowired
	CommunityRepository communityRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public List<Community> findByUserId(String userId) {
		List<Community> communities = new ArrayList<Community>();

		Criteria criteria = new Criteria().where("users").in(userId);
		Query query = new Query();
		query.addCriteria(criteria);
		communities = mongoTemplate.find(query, Community.class);

		//		for (Community community : communityRepository.findAll()) {
		//			if (community.getUsers().contains(userId)) {
		//				communities.add(community);
		//			}
		//		}

		return communities;
	}

	@Override
	public List<String> getCommunityIdsForUser(String userId) {
		List<String> communityIds = new ArrayList<String>();
		Criteria criteria = new Criteria().where("users").in(userId);
		Query query = new Query();
		query.addCriteria(criteria);
		for (Community community : mongoTemplate.find(query, Community.class)) {
			if (!communityIds.contains(community.getId())) {
				communityIds.add(community.getId());
			}
		}

		return communityIds;
	}
}
