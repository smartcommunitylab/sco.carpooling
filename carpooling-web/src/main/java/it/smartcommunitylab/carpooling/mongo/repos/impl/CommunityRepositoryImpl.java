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
import it.smartcommunitylab.carpooling.model.Travel;
import it.smartcommunitylab.carpooling.mongo.repos.CommunityRepoCustom;
import it.smartcommunitylab.carpooling.mongo.repos.CommunityRepository;
import it.smartcommunitylab.carpooling.utils.CarPoolingUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.Sphere;
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

		// for (Community community : communityRepository.findAll()) {
		// if (community.getUsers().contains(userId)) {
		// communities.add(community);
		// }
		// }

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

	@Override
	public List<Community> searchCommunity(String location, String searchText) {

		List<Community> communities = new ArrayList<Community>();
		double lat = -1, lon = -1;

		if (location != null && !location.isEmpty() && location.indexOf(",") > -1) {
			String[] coorindates = location.split(",");
			if (coorindates.length == 2) {
				lat = Double.parseDouble(coorindates[0].trim());
				lon = Double.parseDouble(coorindates[1].trim());
			}
		}

		if (lat > 0 && lon > -1) {

			Point searchLocation = new Point(lat, lon);
			Circle circleFrom = new Circle(searchLocation, CarPoolingUtils.communitySearchRange / 6371);
			Sphere sphereFrom = new Sphere(circleFrom);

			Criteria criteriaF = new Criteria().where("zone.coordinates").within(sphereFrom);

			// query.
			Query query = new Query();
			// add criterias.
			query.addCriteria(criteriaF);

			// if (searchText != null && !searchText.isEmpty()) {
			// Criteria criteriaText = new
			// Criteria().where("zone.name").is(searchText);
			// query.addCriteria(criteriaText);
			// }

			List<Community> matchedCommunity = mongoTemplate.find(query, Community.class);
			communities.addAll(matchedCommunity);

		}

		if (searchText != null && !searchText.isEmpty()) {

			Criteria criteriaText = new Criteria().where("zone.name").regex(Pattern.compile(searchText, Pattern.CASE_INSENSITIVE));
			// query.
			Query query = new Query();
			// add criterias.
			query.addCriteria(criteriaText);

			List<Community> matchedCommunity = mongoTemplate.find(query, Community.class);
			
			for (Community comm: matchedCommunity) {
				if (!communities.contains(comm)) {
					communities.add(comm);
				}
			}
//			communities.addAll(matchedCommunity);
		}

		return communities;
	}
}
