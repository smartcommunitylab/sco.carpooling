package it.smartcommunitylab.carpooling.mongo.repos.impl;

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
		//		query.addCriteria(communityCriteria);
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

		/** community match. **/
		for (TravelRequest req : matchedRequest) {
			List<String> requesterCommIds = communityRepository.getCommunityIdsForUser(req.getUserId());
			requesterCommIds.retainAll(travel.getCommunityIds());

			if (requesterCommIds.size() > 0) {
				matchingRequest.add(req);
			}
		}

		return matchingRequest;

	}

}
