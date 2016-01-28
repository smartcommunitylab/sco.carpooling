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

import it.smartcommunitylab.carpooling.model.RecurrentTravel;
import it.smartcommunitylab.carpooling.model.Travel;
import it.smartcommunitylab.carpooling.model.TravelRequest;
import it.smartcommunitylab.carpooling.mongo.repos.CommunityRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRequestRepositoryCustom;
import it.smartcommunitylab.carpooling.utils.CarPoolingUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.Sphere;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class TravelRequestRepositoryImpl implements TravelRequestRepositoryCustom {

	@Autowired
	MongoTemplate mongoTemplate;
	@Autowired
	private CommunityRepository communityRepository;

	@Override
	public List<TravelRequest> findAllMatchTravelRequest(Travel travel) {

		List<TravelRequest> matchingRequest = new ArrayList<TravelRequest>();

		/** common criteria. **/
		Criteria commonCriteria = new Criteria().where("monitored").is(true);

		/** community criteria. **/
		Criteria communityCriteria = new Criteria().where("communityIds").in(travel.getCommunityIds());
		
		/** zone. **/
		Point pFrom = new Point(travel.getFrom().getLatitude(), travel.getFrom().getLongitude());
		Circle circleFrom = new Circle(pFrom, travel.getFrom().getRange() / 6371);
		Sphere sphereFrom = new Sphere(circleFrom);
		Point pTo = new Point(travel.getTo().getLatitude(), travel.getTo().getLongitude());
		Circle circleTo = new Circle(pTo, travel.getTo().getRange() / 6371);
		Sphere sphereTo = new Sphere(circleTo);
		Criteria zoneCriteria = new Criteria().where("from.coordinates").within(sphereFrom).and("to.coordinates")
				.within(sphereTo);

		Query query = new Query();
		query.addCriteria(commonCriteria);
		query.addCriteria(communityCriteria);
		query.addCriteria(zoneCriteria);

		/** non recurrent time travels. **/
		if (travel.getWhen() > 0L) {
			Date reqDate = new Date(travel.getWhen());
			// match +-1hr.
			Date timePlusOneHour = CarPoolingUtils.getTimeByOffset(reqDate, 1);
			Date timeMinusOneHour = CarPoolingUtils.getTimeByOffset(reqDate, -1);
			Criteria nonRecurr = new Criteria().where("when").gte(timeMinusOneHour.getTime())
					.lte(timePlusOneHour.getTime());
			query.addCriteria(nonRecurr);
		}

		List<TravelRequest> matchedRequest = mongoTemplate.find(query, TravelRequest.class);

//		/** community match. **/
//		for (TravelRequest req : matchedRequest) {
//			List<String> requesterCommIds = communityRepository.getCommunityIdsForUser(req.getUserId());
//			requesterCommIds.retainAll(travel.getCommunityIds());
//
//			if (requesterCommIds.size() > 0) {
//				matchingRequest.add(req);
//			}
//		}

		return matchingRequest;

	}

	@Override
	public List<TravelRequest> findAllMatchTravelRequest(RecurrentTravel recurrentTravel) {

		List<TravelRequest> matchingRequest = new ArrayList<TravelRequest>();

		/** common criteria. **/
		Criteria commonCriteria = new Criteria().where("monitored").is(true);
		
		/** community criteria. **/
		Criteria communityCriteria = new Criteria().where("communityIds").in(recurrentTravel.getCommunityIds());

		/** zone. **/
		Point pFrom = new Point(recurrentTravel.getFrom().getLatitude(), recurrentTravel.getFrom().getLongitude());
		Circle circleFrom = new Circle(pFrom, recurrentTravel.getFrom().getRange() / 6371);
		Sphere sphereFrom = new Sphere(circleFrom);
		Point pTo = new Point(recurrentTravel.getTo().getLatitude(), recurrentTravel.getTo().getLongitude());
		Circle circleTo = new Circle(pTo, recurrentTravel.getTo().getRange() / 6371);
		Sphere sphereTo = new Sphere(circleTo);
		Criteria zoneCriteria = new Criteria().where("from.coordinates").within(sphereFrom).and("to.coordinates")
				.within(sphereTo);

		Query query = new Query();
		query.addCriteria(commonCriteria);
		query.addCriteria(communityCriteria);
		query.addCriteria(zoneCriteria);

		/**
		 * Query: {
			"monitored": true,
			"communityIds": {
				"$in": ["cPCommunity1",
					"cPCommunity2"]
			},
			"from.coordinates": {
				"$within": {
				$java: org.springframework.data.mongodb.core.query.GeoCommand@18ba2b6b
				}
			},
			"to.coordinates": {
				"$within": {
					$java: org.springframework.data.mongodb.core.query.GeoCommand@66788a7b
				}
			}
		}
		 */
		
		List<TravelRequest> matchedRequest = mongoTemplate.find(query, TravelRequest.class);

		/** time,community match. **/
		for (TravelRequest req : matchedRequest) {
			// match time with recurrent travel.
			Date reqDate = new Date(req.getWhen());
			int travelReqHour = CarPoolingUtils.getHour(reqDate);
			int travelReqDOM = CarPoolingUtils.getDayOfMonth(reqDate);
			int travelReqDOW = CarPoolingUtils.getDayOfWeek(reqDate);

			int hourOfRecurrTravel = recurrentTravel.getRecurrency().getTime();
			List<Integer> dOWRecurrTravel = recurrentTravel.getRecurrency().getDays();
			List<Integer> dOMRecurrTravel = recurrentTravel.getRecurrency().getDates();

			if ( ((travelReqHour <= hourOfRecurrTravel + 1) && (travelReqHour >= hourOfRecurrTravel - 1))
					&& ((!dOWRecurrTravel.isEmpty() && dOWRecurrTravel.contains(travelReqDOW))
							| (!dOMRecurrTravel.isEmpty() && dOMRecurrTravel.contains(travelReqDOM)))) {
				matchingRequest.add(req);
			}
		}

		return matchingRequest;
	}

}
