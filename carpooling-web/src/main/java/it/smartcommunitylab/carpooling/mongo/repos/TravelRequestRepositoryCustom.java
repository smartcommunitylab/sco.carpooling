package it.smartcommunitylab.carpooling.mongo.repos;

import it.smartcommunitylab.carpooling.model.Travel;
import it.smartcommunitylab.carpooling.model.TravelRequest;

import java.util.List;

public interface TravelRequestRepositoryCustom {

	List<TravelRequest> findAllMatchTravelRequest(Travel travel);

}
