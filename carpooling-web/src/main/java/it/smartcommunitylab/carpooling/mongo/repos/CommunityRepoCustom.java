package it.smartcommunitylab.carpooling.mongo.repos;

import it.smartcommunitylab.carpooling.model.Community;

import java.util.List;

public interface CommunityRepoCustom {

	List<Community> findByUserId(String userId);
	List<String> getCommunityIdsForUser(String userId);
}
