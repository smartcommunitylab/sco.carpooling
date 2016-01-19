package it.smartcommunitylab.carpooling.mongo.repos.impl;

import it.smartcommunitylab.carpooling.model.RecurrentTravel;
import it.smartcommunitylab.carpooling.mongo.repos.RecurrentTravelRepository;
import it.smartcommunitylab.carpooling.mongo.repos.RecurrentTravelRepositoryCustom;
import it.smartcommunitylab.carpooling.utils.CarPoolingUtils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class RecurrentTravelRepositoryImpl implements RecurrentTravelRepositoryCustom {

	@Autowired
	RecurrentTravelRepository recurrentTravelRepository;

	@Autowired
	MongoTemplate mongoTemplate;

	@Override
	public List<RecurrentTravel> searchTravelsToExtend(int window) {

		List<RecurrentTravel> travelsToExtend = new ArrayList<RecurrentTravel>();

		Long threshold = CarPoolingUtils.adjustNumberOfDaysToWhen(System.currentTimeMillis(), window);

		Criteria criteria = new Criteria().where("lastInstance").lte(threshold);

		Query query = new Query();
		query.addCriteria(criteria);

		travelsToExtend = mongoTemplate.find(query, RecurrentTravel.class);

		return travelsToExtend;
	}

}
