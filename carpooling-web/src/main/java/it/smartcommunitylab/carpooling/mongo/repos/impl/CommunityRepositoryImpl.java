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

import com.mongodb.BasicDBObject;

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

}
