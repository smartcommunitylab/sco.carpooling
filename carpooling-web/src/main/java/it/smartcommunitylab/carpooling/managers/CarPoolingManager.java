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
import it.smartcommunitylab.carpooling.exceptions.CarPoolingCustomException;
import it.smartcommunitylab.carpooling.model.Auto;
import it.smartcommunitylab.carpooling.model.Booking;
import it.smartcommunitylab.carpooling.model.Community;
import it.smartcommunitylab.carpooling.model.Discussion;
import it.smartcommunitylab.carpooling.model.GameProfile;
import it.smartcommunitylab.carpooling.model.Message;
import it.smartcommunitylab.carpooling.model.Notification;
import it.smartcommunitylab.carpooling.model.Travel;
import it.smartcommunitylab.carpooling.model.TravelProfile;
import it.smartcommunitylab.carpooling.model.TravelRequest;
import it.smartcommunitylab.carpooling.model.User;
import it.smartcommunitylab.carpooling.model.Zone;
import it.smartcommunitylab.carpooling.mongo.repos.CommunityRepository;
import it.smartcommunitylab.carpooling.mongo.repos.DiscussionRepository;
import it.smartcommunitylab.carpooling.mongo.repos.NotificationRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRequestRepository;
import it.smartcommunitylab.carpooling.mongo.repos.UserRepository;
import it.smartcommunitylab.carpooling.notification.SendPushNotification;
import it.smartcommunitylab.carpooling.utils.CarPoolingUtils;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
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
	private UserRepository userRepository;
	@Autowired
	private DiscussionRepository discussionRepository;
	@Autowired
	private MobilityPlanner mobilityPlanner;
	@Autowired
	private NotificationRepository notificationRepository;
	@Autowired
	private SendPushNotification sendPushNotification;

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
			// make sure if its the logged in user.
			travelRequest.setUserId(userId);
			travelRequestRepository.save(travelRequest);
		}

		return searchTravels;
	}

	public Travel saveTravel(Travel travel, String userId) throws CarPoolingCustomException {

		// search for plan.
		List<Itinerary> itns = mobilityPlanner.plan(travel);

		if (!itns.isEmpty()) {
			
			String fromName = "";
			String toName = "";
			String fromAddr = "";
			String toAddr = "";
			
			travel.setUserId(userId);
			travel.setRoute(itns.get(0));
			travel.setActive(true);
			
			if (travel.getFrom().getName() != null && !travel.getFrom().getName().isEmpty()) {
				fromName = travel.getFrom().getName(); 
			}
					
			if (travel.getFrom().getAddress() != null && !travel.getFrom().getAddress().isEmpty()) {
				fromAddr = travel.getFrom().getAddress();
			}
					
			if (travel.getTo().getName() != null && !travel.getTo().getName().isEmpty()) {
				toName = travel.getTo().getName();
			}
			
			if (travel.getTo().getAddress() != null && !travel.getTo().getAddress().isEmpty()) {
				toAddr = travel.getTo().getAddress();
			}
			// from.
			Zone updateFrom = new Zone(fromName, fromAddr, travel.getFrom().getLatitude(), travel.getFrom().getLongitude(), travel
					.getFrom().getRange());
			travel.setFrom(updateFrom);
			// to
			Zone updateTo = new Zone(toName, toAddr, travel.getTo().getLatitude(), travel.getTo().getLongitude(), travel
					.getTo().getRange());
			travel.setTo(updateTo);
			
			for (Community community : communityRepository.findByUserId(userId)) {
				if (!travel.getCommunityIds().contains(community.getId())) {
					travel.getCommunityIds().add(community.getId());
				}
			}
			travelRepository.save(travel);

			// loop on all trip request and check if this new travel matches any of those.
			for (TravelRequest travelRequest : travelRequestRepository.findAllMatchTravelRequest(travel)) {
				String travelRequestId = travelRequest.getId();
				String targetUserId = travelRequest.getUserId();
				Map<String, String> data = new HashMap<String, String>();
				data.put("travelRequestId", travelRequestId);
				Notification tripAvailability = new Notification(targetUserId,
						CarPoolingUtils.NOTIFICATION_AVALIABILITY, data, false, travel.getId(),
						System.currentTimeMillis());
				notificationRepository.save(tripAvailability);
				// notify via parse.
				try {
					sendPushNotification.sendNotification(targetUserId, tripAvailability);
				} catch (JSONException e) {
					throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
				}
			}
		} else {
			throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "travel itinerary not found");
		}

		return travel;
	}

	public Travel bookTrip(String travelId, Booking reqBooking, String userId) throws CarPoolingCustomException {

		Travel travel = travelRepository.findOne(travelId);

		if (travel != null) {
			if (CarPoolingUtils.isValidUser(travel, userId, reqBooking)) {
				if (travel.getRecurrency() != null) { // recurrent travel.
					if (CarPoolingUtils.ifBookableRecurr(travel, reqBooking, userId)) {
						travel = CarPoolingUtils.updateTravel(travel, reqBooking, userId);
						travelRepository.save(travel);
					} else {
						throw new CarPoolingCustomException(HttpStatus.PRECONDITION_FAILED.value(),
								"travel not bookable.");
					}

				} else if (!reqBooking.isRecurrent()) {
					//non-recurrent travel + non-recurrent requested booking.
					if (CarPoolingUtils.ifBookable(travel, reqBooking, userId)) {
						// update traveller.
						travel = CarPoolingUtils.updateTravel(travel, reqBooking, userId);
						travelRepository.save(travel);
					} else {
						throw new CarPoolingCustomException(HttpStatus.PRECONDITION_FAILED.value(),
								"travel not bookable.");
					}
				}
				if (travel != null) {
					String targetUserId = travel.getUserId();
					Map<String, String> data = new HashMap<String, String>();
					data.put("senderId", userId);
					User user = userRepository.findOne(userId);
					data.put("senderFullName", user.fullName());
					Notification tripAvailability = new Notification(targetUserId,
							CarPoolingUtils.NOTIFICATION_BOOKING, data, false, travel.getId(),
							System.currentTimeMillis());
					notificationRepository.save(tripAvailability);
					// notify via parse.
					try {
						sendPushNotification.sendNotification(targetUserId, tripAvailability);
					} catch (JSONException e) {
						throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
					}	
				}
			} else {
				throw new CarPoolingCustomException(HttpStatus.FORBIDDEN.value(), "user not valid.");
			}
		} else {
			throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "travel not found.");
		}

		return travel;
	}

	/**
	 * @param tripId
	 * @return
	 * @throws CarPoolingCustomException 
	 */
	public Travel getTrip(String tripId) throws CarPoolingCustomException {
		Travel travel = travelRepository.findOne(tripId);

		if (travel != null) {
			return travel;
		}
		throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "travel not found.");
	}

	public Travel acceptTrip(String travelId, Booking booking, String userId) throws CarPoolingCustomException {

		Travel travel = travelRepository.findTravelByIdAndDriverId(travelId, userId);
		//travelRepository.findOne(travelId);

		boolean found = false;

		if (travel != null) {
			for (Booking book : travel.getBookings()) {
				if (book.equals(booking)) {
					book.setAccepted(booking.getAccepted());
					found = true;
					break;
				}
			}

			if (found) {
				travelRepository.save(travel);
				String targetUserId = booking.getTraveller().getUserId();
				Map<String, String> data = new HashMap<String, String>();
				data.put("status", ""+booking.getAccepted());
				Notification tripAvailability = new Notification(targetUserId,
						CarPoolingUtils.NOTIFICATION_CONFIRM, data, false, travel.getId(),
						System.currentTimeMillis());
				notificationRepository.save(tripAvailability);
				try {
					sendPushNotification.sendNotification(targetUserId, tripAvailability);
				} catch (JSONException e) {
					throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
				}	
			} else {
				throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "booking not found");
			}
		} else {
			throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "travel not found");
		}

		return travel;
	}

	public List<Community> readCommunities(String userId) {

		List<Community> communities = new ArrayList<Community>();
		communities = communityRepository.findByUserId(userId);

		return communities;
	}

	public TravelProfile saveTravelProfile(TravelProfile travelProfile, String userId) {

		User user = userRepository.findOne(userId);
		TravelProfile saveProfile = null;

		if (user != null) {
			user.setTravelProfile(travelProfile);
			user = userRepository.save(user);
			saveProfile = user.getTravelProfile();
		}

		return saveProfile;

	}

	public TravelProfile readTravelProfile(String userId) throws CarPoolingCustomException {

		TravelProfile travelProfile = null;
		User user = userRepository.findOne(userId);

		if (user != null) {
			if (!user.getTravelProfile().getRoutes().isEmpty()) {
				travelProfile = user.getTravelProfile();	
			}
			
		} else {
			throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "user not found");
		}

		return travelProfile;

	}

	public Map<String, String> ratePassenger(String userId, String passengerId, int rating) {

		Map<String, String> errorMap = new HashMap<String, String>();

		if (userId.equalsIgnoreCase(passengerId)) {

			errorMap.put(CarPoolingUtils.ERROR_CODE, String.valueOf(HttpStatus.FORBIDDEN.value()));
			errorMap.put(CarPoolingUtils.ERROR_MSG, "passenger cannot self rate.");

			return errorMap;

		}

		User passenger = userRepository.findOne(passengerId);

		if (passenger != null) {

			GameProfile gameProfile = passenger.getGameProfile();

			if (gameProfile != null) {
				gameProfile.getPassengerRatings().put(userId, rating);
				recalculateRatings(passenger);
			} else {
				errorMap.put(CarPoolingUtils.ERROR_CODE, String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
				errorMap.put(CarPoolingUtils.ERROR_MSG, "passenger has null game profile.");
			}
		} else {

			errorMap.put(CarPoolingUtils.ERROR_CODE, String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
			errorMap.put(CarPoolingUtils.ERROR_MSG, "passenger does not exist.");

		}

		return errorMap;

	}

	public void recalculateRatings(User user) {

		GameProfile gameProfile = user.getGameProfile();

		double totalPRater = 0;
		double totalPRating = 0;

		for (String key : gameProfile.getPassengerRatings().keySet()) {

			totalPRating = totalPRating + gameProfile.getPassengerRatings().get(key);
			totalPRater++;
		}

		gameProfile.setPassengerRating(totalPRating / totalPRater);

		double totalDRater = 0;
		double totalDRating = 0;

		for (String key : gameProfile.getDriverRatings().keySet()) {

			totalDRating = totalDRating + gameProfile.getDriverRatings().get(key);
			totalDRater++;
		}

		gameProfile.setDriverRating(totalDRating / totalDRater);

		user.setGameProfile(gameProfile);

		userRepository.save(user);

	}

	public Map<String, String> rateDriver(String userId, String driverId, int rating) {

		Map<String, String> errorMap = new HashMap<String, String>();

		if (userId.equalsIgnoreCase(driverId)) {

			errorMap.put(CarPoolingUtils.ERROR_CODE, String.valueOf(HttpStatus.FORBIDDEN.value()));
			errorMap.put(CarPoolingUtils.ERROR_MSG, "driver cannot self rate.");

			return errorMap;
		}

		User driver = userRepository.findOne(driverId);

		if (driver != null) {
			GameProfile gameProfile = driver.getGameProfile();

			if (gameProfile != null) {

				gameProfile.getDriverRatings().put(userId, rating);
				recalculateRatings(driver);

			} else {

				errorMap.put(CarPoolingUtils.ERROR_CODE, String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
				errorMap.put(CarPoolingUtils.ERROR_MSG, "driver has null game profile.");

			}

		} else {

			errorMap.put(CarPoolingUtils.ERROR_CODE, String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
			errorMap.put(CarPoolingUtils.ERROR_MSG, "driver does not exist.");

		}

		return errorMap;
	}

	public Map<String, String> updateAutoInfo(String userId, Auto auto) {

		Map<String, String> errorMap = new HashMap<String, String>();

		User driver = userRepository.findOne(userId);

		if (driver != null) {
			if (!auto.getDescription().isEmpty() && auto.getPosts() > -1) {
				driver.setAuto(auto);
			} else {
				driver.setAuto(null);
			}
			userRepository.save(driver);
		} else {
			errorMap.put(CarPoolingUtils.ERROR_CODE, String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
			errorMap.put(CarPoolingUtils.ERROR_MSG, "driver does not exist.");

		}

		return errorMap;
	}

	public Map<String, String> sendMessage(String userId, String travelId, Message message) {

		Map<String, String> status = new HashMap<String, String>();

		try {
			Discussion discussion = discussionRepository.findOne(travelId);

			if (discussion == null) {
				discussion = new Discussion();
				discussion.setTravelId(travelId);
			}

			discussion.getMessages().add(message);
			discussionRepository.save(discussion);
			
			String targetUserId = message.getTargetUserId();
			Map<String, String> data = new HashMap<String, String>();
			data.put("senderId", userId);
			User user = userRepository.findOne(userId);
			data.put("senderFullName", user.fullName());
			data.put("message", message.getMessage());
			Notification tripAvailability = new Notification(targetUserId,
					CarPoolingUtils.NOTIFICATION_CHAT, data, false, travelId,
					System.currentTimeMillis());
			notificationRepository.save(tripAvailability);
			// notify via parse.
			try {
				sendPushNotification.sendNotification(targetUserId, tripAvailability);
			} catch (JSONException e) {
				throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
			}	


		} catch (Exception e) {
			status.put(CarPoolingUtils.ERROR_CODE, String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
			status.put(CarPoolingUtils.ERROR_MSG, e.getMessage());
		}

		return status;

	}

	public Discussion readDiscussion(String userId, String travelId, String targetUserId) {

		Discussion response = new Discussion();
		response.setTravelId(travelId);
		response.setPersonName(userRepository.findOne(targetUserId).fullName());

		Discussion discussion = discussionRepository.findOne(travelId);

		if (discussion != null) {

			// msgs are ordered by the insertion at server side, since it can be sent from diff timezones.
			for (Message msg : discussion.getMessages()) {
				if ((msg.getUserId().equalsIgnoreCase(userId) && (msg.getTargetUserId().equalsIgnoreCase(targetUserId)))
						| (msg.getTargetUserId().equalsIgnoreCase(userId) && msg.getUserId().equalsIgnoreCase(
								targetUserId))) { // targetUserId == userId.(for drivers) or they understand it from bookings
					response.getMessages().add(msg);
				}
			}

		}

		return response;
	}

	/**
	 * read user.
	 * @param userId
	 * @return
	 */
	public User readUser(String userId) {
		User user = userRepository.findOne(userId);
		return user;
	}

	/**
	 * read notifications.
	 * @param userId
	 * @param start
	 * @param count
	 * @return
	 */
	public List<Notification> readNotifications(String userId, int start, int count) {

		Page<Notification> notifications = notificationRepository.findByTargetUserId(userId, new PageRequest(start,
				count, Direction.DESC, "timestamp"));

		return notifications.getContent();

	}

	/**
	 * mark notification as read.
	 * @param id
	 * @return
	 */
	public Map<String, String> markNotification(String id) {

		Map<String, String> errorMap = new HashMap<String, String>();

		Notification notification = notificationRepository.findOne(id);

		if (notification != null) {
			notification.setStatus(true);
			notificationRepository.save(notification);
		} else {
			errorMap.put(CarPoolingUtils.ERROR_CODE, String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
			errorMap.put(CarPoolingUtils.ERROR_MSG, "notification does not exist.");

		}

		return errorMap;
	}

	/**
	 * delete notification id.
	 * @param notificationId
	 * @return
	 */
	public Map<String, String> deleteNotification(String notificationId) {
		
		Map<String, String> errorMap = new HashMap<String, String>();

		Notification notification = notificationRepository.findOne(notificationId);

		if (notification != null) {
			notificationRepository.delete(notification);
		} else {
			errorMap.put(CarPoolingUtils.ERROR_CODE, String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
			errorMap.put(CarPoolingUtils.ERROR_MSG, "notification does not exist.");

		}

		return errorMap;
	}

	/**
	 * delete travel request.
	 * @param travelRequestId
	 * @return
	 */
	public Map<String, String> deleteTravelRequest(String id, String ownerId) {

		Map<String, String> errorMap = new HashMap<String, String>();

		TravelRequest travelRequest = travelRequestRepository.findTravelRequestIdAndUserId(id, ownerId);

		if (travelRequest != null) {
			travelRequestRepository.delete(travelRequest);
		} else {
			errorMap.put(CarPoolingUtils.ERROR_CODE, String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
			errorMap.put(CarPoolingUtils.ERROR_MSG, "travelRequest does not exist.");

		}

		return errorMap;
	}


}