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

package it.smartcommunitylab.carpooling.mongo.repos;

import it.smartcommunitylab.carpooling.model.Travel;
import it.smartcommunitylab.carpooling.model.TravelRequest;

import java.util.List;

public interface TravelRepositoryCustom {

	List<Travel> findTravelByPassengerId(String userId, int start, int count, Long from, Long to, int order,
			Boolean boarded, Boolean accepted, String communityId);
	
	List<Travel> findTravelByDriverId(String userid, int start, int count, Long from, Long to, int order);
	
	List<Travel> findTravelByPassengerId(String userId);

	List<Travel> getAllMatchedCommunityTravels(List<String> userCommunityIds);

	List<Travel> getAllMatchedZoneTravels(TravelRequest travelRequest);

	List<Travel> getAllMatchedTimeTravels(TravelRequest travelRequest);

	List<Travel> searchTravels(TravelRequest travelRequest);
	
	List<Travel> searchCommunityTravels(String communityId, Long timeInMillies);

	List<Travel> searchCompletedTravels(Long timeInMillies);
	
	Travel findOneInstanceOfRecurrTravel(TravelRequest travelRequest, String reccurentTravelId);
	
	List<Travel> findFutureInstanceOfRecurrTravel(String reccurentId);
}
