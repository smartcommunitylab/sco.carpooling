package it.smartcommunitylab.carpooling.mongo.repos;

import it.smartcommunitylab.carpooling.model.RecurrentTravel;

import java.util.List;

public interface RecurrentTravelRepositoryCustom {

	List<RecurrentTravel> searchTravelsToExtend(int window);
}
