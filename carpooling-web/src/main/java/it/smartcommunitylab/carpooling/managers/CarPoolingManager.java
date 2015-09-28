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

package it.smartcommunitylab.carpooling.managers;

import it.sayservice.platform.smartplanner.data.message.Itinerary;
import it.smartcommunitylab.carpooling.model.Community;
import it.smartcommunitylab.carpooling.model.Travel;
import it.smartcommunitylab.carpooling.model.Travel.Booking;
import it.smartcommunitylab.carpooling.model.TravelRequest;
import it.smartcommunitylab.carpooling.mongo.repos.CommunityRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRequestRepository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author nawazk
 *
 */
@Component
public class CarPoolingManager {

	@Autowired
	private TravelRequestRepository travelRequestRepository;
	@Autowired
	private TravelRepository travelRepository;
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private MobilityPlanner mobilityPlanner;

	public List<TravelRequest> getTravelRequest(String userId) {
		return travelRequestRepository.findByUserId(userId);

	}

	public List<TravelRequest> getMonitoredTravelRequest(String userId) {
		return travelRequestRepository.findMonitoredTravelRequest(userId);
	}

	public void saveTravelRequest(TravelRequest travelRequest) {
		travelRequestRepository.save(travelRequest);
	}

	public List<Travel> getPassengerTrips(String passengerId) {
		return travelRepository.findTravelByPassengerId(passengerId);
	}

	public List<Travel> getDriverTrips(String userId) {
		return travelRepository.findTravelByDriverId(userId);
	}

	public List<Travel> searchTravels(TravelRequest travelRequest, String userId) {
		List<Travel> searchTravels = new ArrayList<Travel>();

		List<String> commIdsForUser = communityRepository.getCommunityIdsForUser(userId);

		searchTravels = travelRepository.searchTravels(commIdsForUser, travelRequest);

		if (travelRequest.isMonitored()) {
			travelRequestRepository.save(travelRequest);
		}

		return searchTravels;
	}
	

	public Travel saveTravel(Travel travel, String userId) {

		// search for plan.
		List<Itinerary> itns = mobilityPlanner.plan(travel);

		if (!itns.isEmpty()) {
			travel.setUserId(userId);
			travel.setRoute(itns.get(0));
			travel.setActive(true);
			for (Community community : communityRepository.findByUserId(userId)) {
				if (!travel.getCommunityIds().contains(community.getId())) {
					travel.getCommunityIds().add(community.getId());
				}
			}
		}
		travelRepository.save(travel);

		return travel;
	}

	public Travel bookTrip(String tripId, Booking booking, String userId) {
		// TODO Auto-generated method stub
		return null;
	}

}
