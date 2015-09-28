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

import it.smartcommunitylab.carpooling.model.Travel;
import it.smartcommunitylab.carpooling.model.Travel.Booking;
import it.smartcommunitylab.carpooling.model.TravelRequest;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRepositoryCustom;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.Sphere;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class TravelRepositoryImpl implements TravelRepositoryCustom {

	@Autowired
	TravelRepository travelRepository;

	@Autowired
	MongoTemplate mongoTemplate;

	@Override
	public List<Travel> findTravelByPassengerId(String userId) {
		List<Travel> travelsForPassenger = new ArrayList<Travel>();

		// check if bookings within travel has travellers with userId
		// Criteria criteria = new Criteria();
		// Query query = new Query();
		// criteria.where("booking.traveller.userId").is(userId);
		// query.addCriteria(criteria);
		// travelsForPassenger = mongoTemplate.find(query, Travel.class);

		for (Travel travel : travelRepository.findAll()) {
			for (Booking booking : travel.getBookings()) {
				if (booking.getTraveller().getUserId().equals(userId)) {
					travelsForPassenger.add(travel);
					break;
				}
			}
		}

		return travelsForPassenger;
	}

	@Override
	public List<Travel> getAllMatchedCommunityTravels(List<String> userCommunityIds) {
		List<Travel> matchedTravels = new ArrayList<Travel>();
		// criteria.
		Criteria criteria = new Criteria().where("communityIds").in(userCommunityIds);
		// query.
		Query query = new Query();
		query.addCriteria(criteria);

		matchedTravels = mongoTemplate.find(query, Travel.class);

		return matchedTravels;
	}

	@Override
	public List<Travel> getAllMatchedZoneTravels(TravelRequest travelRequest) {

		Distance d = new Distance(1, Metrics.KILOMETERS);

		double radius = 1;

		List<Travel> matchedTravels = new ArrayList<Travel>();

		Point pFrom = new Point(travelRequest.getFrom().getLatitude(), travelRequest.getFrom().getLongitude());
		Circle circleFrom = new Circle(pFrom, radius / 6371);
		Sphere sphereFrom = new Sphere(circleFrom);

		Point pTo = new Point(travelRequest.getTo().getLatitude(), travelRequest.getTo().getLongitude());
		Circle circleTo = new Circle(pTo, radius / 6371);
		Sphere sphereTo = new Sphere(circleTo);

		// criterias.
		Criteria criteriaF = new Criteria().where("from.coordinates").within(sphereFrom);
		Criteria criteriaT = new Criteria().where("to.coordinates").within(sphereTo);

		// query.
		Query query = new Query();
		// add criterias.
		query.addCriteria(criteriaF);
		query.addCriteria(criteriaT);

		matchedTravels = mongoTemplate.find(query, Travel.class);

		return matchedTravels;
	}

}
