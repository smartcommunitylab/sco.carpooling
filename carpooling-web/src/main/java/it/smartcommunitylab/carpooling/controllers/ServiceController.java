/*******************************************************************************
 * Copyright 2015 Smart Community Lab
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/

package it.smartcommunitylab.carpooling.controllers;

import it.smartcommunitylab.carpooling.managers.CarPoolingManager;
import it.smartcommunitylab.carpooling.model.Booking;
import it.smartcommunitylab.carpooling.model.Community;
import it.smartcommunitylab.carpooling.model.Response;
import it.smartcommunitylab.carpooling.model.Travel;
import it.smartcommunitylab.carpooling.model.TravelRequest;
import it.smartcommunitylab.carpooling.mongo.repos.CommunityRepository;
import it.smartcommunitylab.carpooling.security.UserCommunitiesSetup;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author nawazk
 *
 */
@Controller
public class ServiceController {

	@Autowired
	private CarPoolingManager carPoolingManager;
	@Autowired
	private UserCommunitiesSetup userCommunitiesSetup;
	@Autowired
	private CommunityRepository communityRepository;

	@PostConstruct
	private void init() {
		for (Community community : userCommunitiesSetup.getUserCommunities()) {
			Community existing = communityRepository.findByIdAndName(community.getId(), community.getName());
			if (existing == null) {
				communityRepository.save(community);
			}
		}

	}

	@RequestMapping(method = RequestMethod.GET, value = "/passenger/trips")
	public @ResponseBody
	Response<List<Travel>> readPassengerTrips() {
		return new Response<List<Travel>>(carPoolingManager.getPassengerTrips(getUserId()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/passenger/monitored")
	public @ResponseBody
	Response<List<TravelRequest>> readPassengerMonitoredRequests() {
		return new Response<List<TravelRequest>>(carPoolingManager.getMonitoredTravelRequest(getUserId()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/driver/trips")
	public @ResponseBody
	Response<List<Travel>> readDriverTrips() {
		return new Response<List<Travel>>(carPoolingManager.getDriverTrips(getUserId()));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/passenger/trips")
	public @ResponseBody
	Response<List<Travel>> searchTrips(@RequestBody TravelRequest travelRequest, HttpServletRequest req) {
		List<Travel> foundTravels = new ArrayList<Travel>();
		foundTravels = carPoolingManager.searchTravels(travelRequest, getUserId());

		return new Response<List<Travel>>(foundTravels);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/driver/trips")
	public @ResponseBody
	Response<Travel> createTrips(@RequestBody Travel travel, HttpServletRequest req) {
		Travel savedTravel = carPoolingManager.saveTravel(travel, getUserId());

		return new Response<Travel>(savedTravel);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/passenger/trips/{tripId}/book")
	public @ResponseBody
	Response<Travel> bookTrip(@PathVariable String tripId, @RequestBody Booking booking, HttpServletRequest req) {
		Travel travel = carPoolingManager.bookTrip(tripId, booking, getUserId());

		return new Response<Travel>(travel);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/passenger/trips/{tripId}/accept")
	public @ResponseBody
	Response<Travel> acceptTrip(@PathVariable String tripId, @RequestBody Booking booking, HttpServletRequest req) {
		Travel travel = carPoolingManager.acceptTrip(tripId, booking, getUserId());

		return new Response<Travel>(travel);
	}

	@ExceptionHandler(Exception.class)
	public @ResponseBody
	Response<Void> handleExceptions(Exception exception) {
		return new Response<Void>(500, exception.getMessage());
	}

	private String getUserId() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : principal.toString();
	}

}
