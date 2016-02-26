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
import it.smartcommunitylab.carpooling.model.TravelRequest;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRepositoryCustom;
import it.smartcommunitylab.carpooling.utils.CarPoolingUtils;
import it.smartcommunitylab.carpooling.utils.Location;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Circle;
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
	public List<Travel> findTravelByPassengerId(String userId, int pageNum, int pageSize, Long from, Long to
			, int order, Boolean boarded, Boolean accepted, String communityId) {
		
		List<Travel> travelsForPassenger = new ArrayList<Travel>();

		Criteria timeC = new Criteria().where("when").gte(from).lte(to);

		Criteria communityC = new Criteria().where("communityIds").in(communityId);

		Criteria elemCriteria = Criteria.where("traveller.userId").is(userId);
		if (boarded != null) {
			if (boarded) elemCriteria = elemCriteria.and("boarded").ne(0);
			else elemCriteria = elemCriteria.and("boarded").is(0);
		}
		if (accepted != null) {
			if (accepted) elemCriteria = elemCriteria.and("accepted").is(1);
			else elemCriteria = elemCriteria.and("accepted").ne(1);
		} else {
			elemCriteria = elemCriteria.and("accepted").ne(-1);
		}
		
		// check if bookings within travel has travellers with userId
		Criteria criteria = Criteria.where("bookings").elemMatch(elemCriteria);

		Query query = new Query();
		
		// (optional) - time.
		if (from != null && from > 0 && to != null && to > 0) {
			query.addCriteria(timeC);

		}
		// (optional) - communityId.
		if (communityId != null && !communityId.isEmpty()) {
			query.addCriteria(communityC);
		}
		query.addCriteria(criteria);
			
		// pagination.
		query.skip(pageNum * pageSize);
		query.limit(pageSize);
		query.with(new Sort((order == -1) ? Sort.Direction.DESC : Sort.Direction.ASC, "when"));
		
		
		/**
		query:
		{
			"bookings": {
				"$elemMatch": {
					"traveller.userId": "52"
				}
			},
			"when": {
				"$gte": 1453762800000,
				"$lte": 1454022000000
			},
			"communityIds": {
				"$in": ["cPCommunity1"]
			}
		}
		**/
		
		travelsForPassenger = mongoTemplate.find(query, Travel.class);

		/**for (Travel travel : travelRepository.findAll()) {
			for (Booking booking : travel.getBookings()) {
				if (booking.getTraveller().getUserId().equals(userId)) {
					travelsForPassenger.add(travel);
					break;
				}
			}
		}**/

		return travelsForPassenger;
	}
	
	@Override
	public List<Travel> findTravelByDriverId(String userId, int pageNum, int pageSize, Long from, Long to, int order) {
		List<Travel> travelsForDriver = new ArrayList<Travel>();

		Criteria timeC = new Criteria().where("when").gte(from).lte(to);

		// check if bookings within travel has travellers with userId
		Criteria criteria = new Criteria().where("userId").is(userId);

		Query query = new Query();
		query.addCriteria(criteria);

		// (optional) - time.
		if (from != null && from > 0 && to != null && to > 0) {
			query.addCriteria(timeC);

		}

		// pagination.
		query.skip(pageNum * pageSize);
		query.limit(pageSize);
		query.with(new Sort((order == -1) ? Sort.Direction.DESC : Sort.Direction.ASC, "when"));

		/**
		 Query:
		 {
			"userId": "53",
			"when": {
				"$gte": 1453762800000,
				"$lte": 1454022000000
			}
		}
		
		*/

		travelsForDriver = mongoTemplate.find(query, Travel.class);

		return travelsForDriver;

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

		List<Travel> matchedTravels = new ArrayList<Travel>();

		Point pFrom = new Point(travelRequest.getFrom().getLatitude(), travelRequest.getFrom().getLongitude());
		Circle circleFrom = new Circle(pFrom, travelRequest.getFrom().getRange() / 6371);
		Sphere sphereFrom = new Sphere(circleFrom);

		Point pTo = new Point(travelRequest.getTo().getLatitude(), travelRequest.getTo().getLongitude());
		Circle circleTo = new Circle(pTo, travelRequest.getTo().getRange() / 6371);
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

	@Override
	public List<Travel> getAllMatchedTimeTravels(TravelRequest travelRequest) {

		List<Travel> matchTravels = new ArrayList<Travel>();

		/** non recurrent time travels. **/
		Date reqDate = new Date(travelRequest.getWhen());
		// match +-1hr.
		Date timePlusOneHour = CarPoolingUtils.getTimeByOffset(reqDate, 1);
		Date timeMinusOneHour = CarPoolingUtils.getTimeByOffset(reqDate, -1);
		// recurrent data.
		int reqDOW = CarPoolingUtils.getDayOfWeek(reqDate);
		int reqDOM = CarPoolingUtils.getDayOfMonth(reqDate);
		int recurrTimePlusOneHour = CarPoolingUtils.getHour(timePlusOneHour);
		int recurrTimeMinusOneHour = CarPoolingUtils.getHour(timeMinusOneHour);

		Criteria nonRecurr = new Criteria().where("when").gte(timeMinusOneHour.getTime())
				.lte(timePlusOneHour.getTime());

		/** recurrent travels. **/
		Criteria criteriaReccurGeneral = new Criteria().where("when").is(0).and("recurrency").exists(true)
				.and("active").is(true).and("recurrency.time").gte(recurrTimeMinusOneHour).lte(recurrTimePlusOneHour);
		Criteria criteriaReccurDOW = new Criteria().where("recurrency.days").in(reqDOW);
		Criteria criteriaRecurrDOM = new Criteria().where("recurrency.dates").in(reqDOM);

		Criteria recurrDOW = new Criteria().andOperator(criteriaReccurGeneral, criteriaReccurDOW);
		Criteria recurrDOM = new Criteria().andOperator(criteriaReccurGeneral, criteriaRecurrDOM);

		Criteria criteria = new Criteria().where("active").is(true).orOperator(nonRecurr, recurrDOW, recurrDOM);

		/**
		Query:
		{
			"active": true,
			"$or": [{
				"when": {
					"$gte": 1443421800000,
					"$lte": 1443429000000
				}
			},
			{
				"$and": [{
					"when": 0,
					"recurrency": {
						"$exists": true
					},
					"active": true,
					"recurrency.time": {
						"$gte": 8,
						"$lte": 10
					}
				},
				{
					"recurrency.days": {
						"$in": [2]
					}
				}]
			},
			{
				"$and": [{
					"when": 0,
					"recurrency": {
						"$exists": true
					},
					"active": true,
					"recurrency.time": {
						"$gte": 8,
						"$lte": 10
					}
				},
				{
					"recurrency.dates": {
						"$in": [28]
					}
				}]
			}]
		}**/

		Query query = new Query();
		query.addCriteria(criteria);
		matchTravels = mongoTemplate.find(query, Travel.class);

		return matchTravels;
	}

	@Override
	public List<Travel> searchTravels(TravelRequest travelRequest) {

		List<Travel> travels = new ArrayList<Travel>();

		Criteria commonCriteria = new Criteria().where("active").is(true);
		
		/** community. **/
		Criteria communityCriteria = null;
		if (travelRequest.getCommunityIds() != null && !travelRequest.getCommunityIds().isEmpty()) {
			communityCriteria = new Criteria().where("communityIds").in(travelRequest.getCommunityIds());
		}
		/** zone. **/
		Point pFrom = new Point(travelRequest.getFrom().getLatitude(), travelRequest.getFrom().getLongitude());
		Circle circleFrom = new Circle(pFrom, travelRequest.getFrom().getRange() / 6371);
		Sphere sphereFrom = new Sphere(circleFrom);
		Point pTo = new Point(travelRequest.getTo().getLatitude(), travelRequest.getTo().getLongitude());
		Circle circleTo = new Circle(pTo, travelRequest.getTo().getRange() / 6371);
		Sphere sphereTo = new Sphere(circleTo);
		Criteria zoneCriteria = new Criteria().where("from.coordinates").within(sphereFrom).and("to.coordinates")
				.within(sphereTo);
		/** recurrent/non trip times. **/
		Date reqDate = new Date(travelRequest.getWhen());
		// match +-1hr.
		Date timePlusOneHour = CarPoolingUtils.getTimeByOffset(reqDate, 1);
		Date timeMinusOneHour = CarPoolingUtils.getTimeByOffset(reqDate, -1);

		// normal.
		Criteria nonRecurr = new Criteria().where("when").gte(timeMinusOneHour.getTime())
				.lte(timePlusOneHour.getTime());
		Query query = new Query();
		query.addCriteria(commonCriteria);
		if (communityCriteria != null) query.addCriteria(communityCriteria);
		query.addCriteria(zoneCriteria);
		query.addCriteria(nonRecurr);
		
		/** Query:                
		{
			"active": true,
			"communityIds": {
				"$in": ["cPCommunity1",
				"cPCommunity2"]
			},
			"from.coordinates": {
				"$within": {
					
				}
			},
			"to.coordinates": {
				"$within": {
					
				}
			},
			"when": {
				"$gte": reqTravel.when - 1hr,
				"$lte": reqTravel.when + 1hr
			}
		} **/
		
		travels = mongoTemplate.find(query, Travel.class);

		/** filter by posts/booking state. **/
		List<Travel> temp = new ArrayList<Travel>();
		temp.addAll(travels);

		for (Travel travel : temp) {

			if (CarPoolingUtils.getNonRecurrentAvailabiliy(travel, travelRequest) < 1) {// travelRequest.getNrOfPost()
				travels.remove(travel);
			}
		}

		return travels;
	}
	
	@Override
	public List<Travel> searchTravelsExtended(TravelRequest travelRequest) {
		
		List<Travel> travels = new ArrayList<Travel>();

		Criteria commonCriteria = new Criteria().where("active").is(true);
	
		/** community. **/
		Criteria communityCriteria = null;
		if (travelRequest.getCommunityIds() != null && !travelRequest.getCommunityIds().isEmpty()) {
			communityCriteria = new Criteria().where("communityIds").in(travelRequest.getCommunityIds());
		}
		
		/** time. **/
		Date reqDate = new Date(travelRequest.getWhen());
		// match +-1hr.
		Date timePlusOneHour = CarPoolingUtils.getTimeByOffset(reqDate, 1);
		Date timeMinusOneHour = CarPoolingUtils.getTimeByOffset(reqDate, -1);
		Criteria timeCriteria = new Criteria().where("when").gte(timeMinusOneHour.getTime())
				.lte(timePlusOneHour.getTime());
		
		Query query = new Query();
		query.addCriteria(commonCriteria);
		query.addCriteria(timeCriteria);
		
		if (communityCriteria != null) {
			query.addCriteria(communityCriteria);
		}
		
		// find set of travels.
		// call a function take takes
		
		
		List<Travel> timeCommunityTravels = mongoTemplate.find(query, Travel.class);
		
		for (Travel checkTravel : timeCommunityTravels) {

			if (isGeoValid(checkTravel, travelRequest)) {
				// seat availability check.
				if (CarPoolingUtils.getNonRecurrentAvailabiliy(checkTravel, travelRequest) > 0) {
					travels.add(checkTravel);
				}

			}
		}
		
		
	
		return travels;
		
	}
	
	private boolean isGeoValid(Travel checkTravel, TravelRequest travelRequest) {
		
		boolean isGeoValid = false;

		Point pFrom = new Point(travelRequest.getFrom().getLatitude(), travelRequest.getFrom().getLongitude());
		double radiusFrom = travelRequest.getFrom().getRange();

		Point pTo = new Point(travelRequest.getTo().getLatitude(), travelRequest.getTo().getLongitude());
		double radiusTo = travelRequest.getTo().getRange();

		String polyLine = checkTravel.getRoute().getLeg().get(0).getLegGeometery().getPoints();

		List<Location> polyLinePoints = CarPoolingUtils.decode(polyLine, 1E-5);

		int sIndex = getNearPointIndexOnPolyLine(polyLinePoints, pFrom, radiusFrom);

		int dIndex = getNearPointIndexOnPolyLine(polyLinePoints, pTo, radiusTo);

		if (sIndex > -1 && dIndex > -1 && sIndex < dIndex) {
			isGeoValid = true;

		}

		return isGeoValid;
	}

	private int getNearPointIndexOnPolyLine(List<Location> polyLinePoints, Point reqPoint, double radiusFrom) {

		int index = -1;

		double distance = 0;

		for (int i = 0; i < polyLinePoints.size(); i++) {

			Location location = polyLinePoints.get(i);

			double temp = CarPoolingUtils.calculateHarvesineDistance(reqPoint.getX(), reqPoint.getY(),
					location.getLatitude(), location.getLongitude());

			if (distance == 0) {
				distance = temp;
			}
			
			if (temp <= distance && temp <= radiusFrom) {
				distance = temp;
				index = i;

			}

		}

		return index;
	}

	@Override
	public List<Travel> searchCommunityTravels(String communityId, Long timeInMillies) {
		
		// eliminate time in past.
		List<Travel> travels = new ArrayList<Travel>();

		Criteria commonCriteria = new Criteria().where("active").is(true);
		/** community. **/
		Criteria communityCriteria = new Criteria().where("communityIds").in(communityId);
		/** recurrent/non trip times. **/
		Date reqDate = new Date(timeInMillies);
		// from the start of day until end of day.
		Date startOfDay = CarPoolingUtils.getStartOfDay(reqDate);
		Date timeEndOfDay = CarPoolingUtils.getEndOfDay(reqDate);
		// normal.
		Criteria timeCriteria = new Criteria().where("when").gte(startOfDay)
				.lte(timeEndOfDay.getTime());

		Query query = new Query();
		query.addCriteria(commonCriteria);
		query.addCriteria(communityCriteria);
		query.addCriteria(timeCriteria);
		// order them by ascending.
		query.with(new Sort(Sort.Direction.ASC, "when"));
		
		/**
		Query:
			{
			"active": true,
			"communityIds": {
				"$in": ["cPCommunity1"]
				},
			"when": {
				"$gte": 1451547360000,
				"$lte": 1451602799999
				}
		},
		Fields: null,
		Sort: {
			"when": 1
		}
		**/

		travels = mongoTemplate.find(query, Travel.class);

		return travels;
	}

	@Override
	public int countTravelByPassengerId(String userId) {

		// check if bookings within travel has travellers with userId
		Criteria criteria = new Criteria().where("bookings").elemMatch(
				Criteria.where("traveller.userId").is(userId).and("boarded").is(1));

		Query query = new Query();
		query.addCriteria(criteria);

		/**
		 Query:{
			{
			"bookings": {
				"$elemMatch": {
					"traveller.userId": "53",
					"boarded": 1
					}
				}
			}
		}
		**/

		return (int)mongoTemplate.count(query, Travel.class);
	}

	@Override
	public int countTravelByDriverId(String userId) {

		// check if there exist bookings with boardings
		Criteria criteria = new Criteria().where("userId").is(userId).and("bookings").elemMatch(
				Criteria.where("boarded").is(1));

		Query query = new Query();
		query.addCriteria(criteria);

		/**
		 Query:{
			{
			"bookings": {
				"$elemMatch": {
					"traveller.userId": "53",
					"boarded": 1
					}
				}
			}
		}
		**/

		return (int)mongoTemplate.count(query, Travel.class);
	}

	@Override
	public List<Travel> searchCompletedTravels(Long timeInMillies) {
		List<Travel> completedTravels = new ArrayList<Travel>();

		Criteria criteria = new Criteria().where("when").lte(timeInMillies);

		Query query = new Query();
		query.addCriteria(criteria);

		completedTravels = mongoTemplate.find(query, Travel.class);

		return completedTravels;
	}

	@Override
	public Travel findOneInstanceOfRecurrTravel(TravelRequest travelRequest, String reccurentTravelId) {

		Date reqDate = new Date(travelRequest.getWhen());
		// match +-1hr.
		Date timePlusOneHour = CarPoolingUtils.getTimeByOffset(reqDate, 1);
		Date timeMinusOneHour = CarPoolingUtils.getTimeByOffset(reqDate, -1);
		
		Criteria criteria = new Criteria().where("recurrentId").is(reccurentTravelId).and("when")
				.gte(timeMinusOneHour.getTime()).lte(timePlusOneHour.getTime());

		Query query = new Query();
		query.addCriteria(criteria);

		return mongoTemplate.findOne(query, Travel.class);
	}

	@Override
	public List<Travel> findFutureInstanceOfRecurrTravel(String reccurentId) {

		Criteria criteria = new Criteria().where("recurrentId").is(reccurentId).and("when")
				.gte(System.currentTimeMillis());

		Query query = new Query();
		query.addCriteria(criteria);

		return mongoTemplate.find(query, Travel.class);

	}




}
