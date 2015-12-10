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

import it.smartcommunitylab.carpooling.exceptions.CarPoolingCustomException;
import it.smartcommunitylab.carpooling.managers.CarPoolingManager;
import it.smartcommunitylab.carpooling.model.Auto;
import it.smartcommunitylab.carpooling.model.Booking;
import it.smartcommunitylab.carpooling.model.Community;
import it.smartcommunitylab.carpooling.model.Discussion;
import it.smartcommunitylab.carpooling.model.Message;
import it.smartcommunitylab.carpooling.model.Notification;
import it.smartcommunitylab.carpooling.model.Response;
import it.smartcommunitylab.carpooling.model.Travel;
import it.smartcommunitylab.carpooling.model.TravelProfile;
import it.smartcommunitylab.carpooling.model.TravelRequest;
import it.smartcommunitylab.carpooling.model.User;
import it.smartcommunitylab.carpooling.mongo.repos.CommunityRepository;
import it.smartcommunitylab.carpooling.security.UserCommunitiesSetup;
import it.smartcommunitylab.carpooling.utils.CarPoolingUtils;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
			//Community existing = communityRepository.findByIdAndName(community.getId(), community.getName());
			//if (existing == null) {
			communityRepository.save(community);
			//}
		}

	}

	@RequestMapping(method = RequestMethod.GET, value = "/api/passenger/trips")
	public @ResponseBody
	Response<List<Travel>> readPassengerTrips() throws CarPoolingCustomException {
		return new Response<List<Travel>>(carPoolingManager.getPassengerTrips(getUserId()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/api/passenger/monitored")
	public @ResponseBody
	Response<List<TravelRequest>> readPassengerMonitoredRequests() throws CarPoolingCustomException {
		return new Response<List<TravelRequest>>(carPoolingManager.getMonitoredTravelRequest(getUserId()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/api/driver/trips")
	public @ResponseBody
	Response<List<Travel>> readDriverTrips() throws CarPoolingCustomException {
		return new Response<List<Travel>>(carPoolingManager.getDriverTrips(getUserId()));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/api/passenger/trips")
	public @ResponseBody
	Response<List<Travel>> searchTrips(@RequestBody TravelRequest travelRequest) throws CarPoolingCustomException {
		return new Response<List<Travel>>(carPoolingManager.searchTravels(travelRequest, getUserId()));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/api/driver/trips")
	public @ResponseBody
	Response<Travel> createTrips(@RequestBody Travel travel) throws CarPoolingCustomException {
		return new Response<Travel>(carPoolingManager.saveTravel(travel, getUserId()));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/api/passenger/trips/{tripId}")
	public @ResponseBody
	Response<Travel> createTrips(@PathVariable String tripId) throws CarPoolingCustomException {
		return new Response<Travel>(carPoolingManager.getTrip(tripId));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/api/passenger/trips/{tripId}/book")
	public @ResponseBody
	Response<Travel> bookTrip(@PathVariable String tripId, @RequestBody Booking booking)
			throws CarPoolingCustomException {
		return new Response<Travel>(carPoolingManager.bookTrip(tripId, booking, getUserId()));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/api/driver/trips/{tripId}/accept")
	public @ResponseBody
	Response<Travel> acceptTrip(@PathVariable String tripId, @RequestBody Booking booking)
			throws CarPoolingCustomException {
		return new Response<Travel>(carPoolingManager.acceptTrip(tripId, booking, getUserId()));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/api/message/{travelId}/send")
	public @ResponseBody
	Response<String> sendMsg(@PathVariable String travelId, @RequestBody Message message)
			throws CarPoolingCustomException {

		Response<String> response = new Response<String>();

		Map<String, String> errorMap = carPoolingManager.sendMessage(getUserId(), travelId, message);

		if (errorMap.isEmpty()) {
			response.setData("message sent successfully.");
		} else if (errorMap.containsKey(CarPoolingUtils.ERROR_CODE)) {
			throw new CarPoolingCustomException(Integer.valueOf(errorMap.get(CarPoolingUtils.ERROR_CODE)),
					errorMap.get(CarPoolingUtils.ERROR_MSG));
		}

		return response;

	}

	@RequestMapping(method = RequestMethod.GET, value = "/api/read/{travelId}/{targetUserId}/discussion")
	public @ResponseBody
	Response<Discussion> readThread(@PathVariable String travelId, @PathVariable String targetUserId)
			throws CarPoolingCustomException {

		Discussion discussion = carPoolingManager.readDiscussion(getUserId(), travelId, targetUserId);

		if (discussion != null) {
			return new Response<Discussion>(discussion);
		} else {
			throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "discussion not found");
		}

	}

	@RequestMapping(method = RequestMethod.GET, value = "/api/read/profile")
	public @ResponseBody
	Response<TravelProfile> readProfile() throws CarPoolingCustomException {

		TravelProfile travelProfile = carPoolingManager.readTravelProfile(getUserId());

		if (travelProfile != null) {
			return new Response<TravelProfile>(travelProfile);
		} else {
			throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "travel profile is null");
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/api/rate/driver/{driverId}/{rating}")
	public @ResponseBody
	Response<String> rateDriver(@PathVariable String driverId, @PathVariable int rating)
			throws CarPoolingCustomException {

		Response<String> response = new Response<String>();

		Map<String, String> errorMap = carPoolingManager.rateDriver(getUserId(), driverId, rating);

		if (errorMap.isEmpty()) {
			response.setData("rating done successfully");
		} else if (errorMap.containsKey(CarPoolingUtils.ERROR_CODE)) {
			throw new CarPoolingCustomException(Integer.valueOf(errorMap.get(CarPoolingUtils.ERROR_CODE)),
					errorMap.get(CarPoolingUtils.ERROR_MSG));
		}

		return response;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/api/rate/passenger/{passengerId}/{rating}")
	public @ResponseBody
	Response<String> ratePassenger(@PathVariable String passengerId, @PathVariable int rating)
			throws CarPoolingCustomException {

		Response<String> response = new Response<String>();

		Map<String, String> errorMap = carPoolingManager.ratePassenger(getUserId(), passengerId, rating);

		if (errorMap.isEmpty()) {
			response.setData("rating done successfully.");
		} else if (errorMap.containsKey(CarPoolingUtils.ERROR_CODE)) {
			throw new CarPoolingCustomException(Integer.valueOf(errorMap.get(CarPoolingUtils.ERROR_CODE)),
					errorMap.get(CarPoolingUtils.ERROR_MSG));
		}

		return response;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/api/save/profile")
	public @ResponseBody
	Response<TravelProfile> saveProfile(@RequestBody TravelProfile profile) throws CarPoolingCustomException {

		TravelProfile travelProfile = carPoolingManager.saveTravelProfile(profile, getUserId());

		if (travelProfile != null) {
			return new Response<TravelProfile>(travelProfile);
		} else {
			throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "travel profile not saved.");
		}

	}

	@RequestMapping(method = RequestMethod.GET, value = "/api/read/communities")
	public @ResponseBody
	Response<List<Community>> readCommunities() throws CarPoolingCustomException {
		return new Response<List<Community>>(carPoolingManager.readCommunities(getUserId()));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/api/save/autoInfo")
	public @ResponseBody
	Response<String> updateAutoInfo(@RequestBody Auto auto) throws CarPoolingCustomException {

		Response<String> response = new Response<String>();

		Map<String, String> errorMap = carPoolingManager.updateAutoInfo(getUserId(), auto);

		if (errorMap.isEmpty()) {
			response.setData("auto information saved successfully");

		} else if (errorMap.containsKey(CarPoolingUtils.ERROR_CODE)) {
			//response.setErrorCode(Integer.valueOf(errorMap.get(CarPoolingUtils.ERROR_CODE)));
			//response.setErrorMessage(errorMap.get(CarPoolingUtils.ERROR_MSG));
			throw new CarPoolingCustomException(Integer.valueOf(errorMap.get(CarPoolingUtils.ERROR_CODE)),
					errorMap.get(CarPoolingUtils.ERROR_MSG));

		}

		return response;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/api/read/user/{userId}")
	public @ResponseBody
	Response<User> readUser(@PathVariable String userId) throws CarPoolingCustomException {

		// perhaps we can add some security here
		// for e.g. (check if logged in user and passed in user matches, send all)
		// else send only restricted information.
		User user = carPoolingManager.readUser(userId);

		if (user != null) {
			return new Response<User>(user);
		} else {
			throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "user not found");
		}

	}

	@RequestMapping(method = RequestMethod.GET, value = "/api/read/notifications/{start}/{count}")
	public @ResponseBody
	Response<List<Notification>> readNotifications(@PathVariable int start, @PathVariable int count)
			throws CarPoolingCustomException {
		return new Response<List<Notification>>(carPoolingManager.readNotifications(getUserId(), start, count));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/api/mark/read/notification/{notificationId}")
	public @ResponseBody
	Response<String> markNotificaton(@PathVariable String notificationId) throws CarPoolingCustomException {

		Response<String> response = new Response<String>();

		Map<String, String> errorMap = carPoolingManager.markNotification(notificationId);

		if (errorMap.isEmpty()) {
			response.setData("notification marked.");
		} else if (errorMap.containsKey(CarPoolingUtils.ERROR_CODE)) {
			throw new CarPoolingCustomException(Integer.valueOf(errorMap.get(CarPoolingUtils.ERROR_CODE)),
					errorMap.get(CarPoolingUtils.ERROR_MSG));
		}

		return response;
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/api/delete/notification/{notificationId}")
	public @ResponseBody
	Response<String> deleteNotification(@PathVariable String notificationId) throws CarPoolingCustomException {

		Response<String> response = new Response<String>();

		Map<String, String> errorMap = carPoolingManager.deleteNotification(notificationId);

		if (errorMap.isEmpty()) {
			response.setData("notification deleted successfully");
		} else if (errorMap.containsKey(CarPoolingUtils.ERROR_CODE)) {
			throw new CarPoolingCustomException(Integer.valueOf(errorMap.get(CarPoolingUtils.ERROR_CODE)),
					errorMap.get(CarPoolingUtils.ERROR_MSG));
		}

		return response;

	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/api/delete/tripRequest/{travelRequestId}")
	public @ResponseBody
	Response<String> deleteTravelRequest(@PathVariable String travelRequestId) throws CarPoolingCustomException {

		Response<String> response = new Response<String>();

		Map<String, String> errorMap = carPoolingManager.deleteTravelRequest(travelRequestId, getUserId());

		if (errorMap.isEmpty()) {
			response.setData("travelRequest deleted successfully");
		} else if (errorMap.containsKey(CarPoolingUtils.ERROR_CODE)) {
			throw new CarPoolingCustomException(Integer.valueOf(errorMap.get(CarPoolingUtils.ERROR_CODE)),
					errorMap.get(CarPoolingUtils.ERROR_MSG));
		}

		return response;
	}

	@ExceptionHandler(Exception.class)
	public @ResponseBody
	Response<Void> handleExceptions(Exception exception, HttpServletResponse response) {
		Response<Void> res = exception instanceof CarPoolingCustomException ? ((CarPoolingCustomException) exception)
				.getBody() : new Response<Void>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
		response.setStatus(res.getErrorCode());
		return res;
	}

	private String getUserId() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : principal.toString();
	}

}
