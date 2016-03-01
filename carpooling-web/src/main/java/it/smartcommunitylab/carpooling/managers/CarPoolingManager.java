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
import it.smartcommunitylab.carpooling.model.Recurrency;
import it.smartcommunitylab.carpooling.model.RecurrentBooking;
import it.smartcommunitylab.carpooling.model.RecurrentTravel;
import it.smartcommunitylab.carpooling.model.Travel;
import it.smartcommunitylab.carpooling.model.TravelProfile;
import it.smartcommunitylab.carpooling.model.TravelRequest;
import it.smartcommunitylab.carpooling.model.User;
import it.smartcommunitylab.carpooling.model.Zone;
import it.smartcommunitylab.carpooling.mongo.repos.CommunityRepository;
import it.smartcommunitylab.carpooling.mongo.repos.DiscussionRepository;
import it.smartcommunitylab.carpooling.mongo.repos.NotificationRepository;
import it.smartcommunitylab.carpooling.mongo.repos.RecurrentTravelRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRequestRepository;
import it.smartcommunitylab.carpooling.mongo.repos.UserRepository;
import it.smartcommunitylab.carpooling.notification.SendPushNotification;
import it.smartcommunitylab.carpooling.utils.CarPoolingUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 
 * @author nawazk
 * 
 */
@Component
public class CarPoolingManager {

	private static final transient Log logger = LogFactory.getLog(CarPoolingManager.class);

	@Autowired
	private TravelRequestRepository travelRequestRepository;
	@Autowired
	private TravelRepository travelRepository;
	@Autowired
	private RecurrentTravelRepository reccurrentTravelRepository;
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

	/**
	 * Save Non Recurrent Travel.
	 * 
	 * @param travel
	 * @param userId
	 * @return Travel
	 * @throws CarPoolingCustomException
	 */
	public Travel saveTravel(Travel travel, String userId) throws CarPoolingCustomException {

		// search for plan.
		List<Itinerary> itns = mobilityPlanner.plan(travel.getFrom(), travel.getTo(), travel.getWhen());

		if (!itns.isEmpty() && hasValidPolyLine(itns.get(0))) {

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
			Zone updateFrom = new Zone(fromName, fromAddr, travel.getFrom().getLatitude(), travel.getFrom()
					.getLongitude(), travel.getFrom().getRange());
			travel.setFrom(updateFrom);
			// to
			Zone updateTo = new Zone(toName, toAddr, travel.getTo().getLatitude(), travel.getTo().getLongitude(),
					travel.getTo().getRange());
			travel.setTo(updateTo);

			if (travel.getCommunityIds().isEmpty()) {
				for (Community community : communityRepository.findByUserId(userId)) {
					if (!travel.getCommunityIds().contains(community.getId())) {
						travel.getCommunityIds().add(community.getId());
					}
				}
			}

			travelRepository.save(travel);

			// loop on all trip request and check if this new travel matches any
			// of those.
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

	/**
	 * Has Valid PolyLine Checker.
	 * @param itinerary
	 * @return
	 */
	private boolean hasValidPolyLine(Itinerary itinerary) {

		boolean validGeometry = false;

		String polyLine = itinerary.getLeg().get(0).getLegGeometery().getPoints();

		if (polyLine != null && !polyLine.isEmpty()) {
			validGeometry = true;

		}

		return validGeometry;
	}

	/**
	 * Save Recurrent Travel.
	 * @param recurrentTravel
	 * @param userId
	 * @return Recurrent Travel.
	 * @throws CarPoolingCustomException
	 */
	public RecurrentTravel saveRecurrentTravel(RecurrentTravel recurrentTravel, String userId)
			throws CarPoolingCustomException {
		// search for plan.
		List<Itinerary> itns = mobilityPlanner.plan(recurrentTravel.getFrom(), recurrentTravel.getTo(),
				recurrentTravel.getWhen());

		if (!itns.isEmpty() && hasValidPolyLine(itns.get(0))) {

			String fromName = "";
			String toName = "";
			String fromAddr = "";
			String toAddr = "";

			recurrentTravel.setUserId(userId);
			recurrentTravel.setRoute(itns.get(0));
			recurrentTravel.setActive(true);

			if (recurrentTravel.getFrom().getName() != null && !recurrentTravel.getFrom().getName().isEmpty()) {
				fromName = recurrentTravel.getFrom().getName();
			}

			if (recurrentTravel.getFrom().getAddress() != null && !recurrentTravel.getFrom().getAddress().isEmpty()) {
				fromAddr = recurrentTravel.getFrom().getAddress();
			}

			if (recurrentTravel.getTo().getName() != null && !recurrentTravel.getTo().getName().isEmpty()) {
				toName = recurrentTravel.getTo().getName();
			}

			if (recurrentTravel.getTo().getAddress() != null && !recurrentTravel.getTo().getAddress().isEmpty()) {
				toAddr = recurrentTravel.getTo().getAddress();
			}
			// from.
			Zone updateFrom = new Zone(fromName, fromAddr, recurrentTravel.getFrom().getLatitude(), recurrentTravel
					.getFrom().getLongitude(), recurrentTravel.getFrom().getRange());
			recurrentTravel.setFrom(updateFrom);
			// to
			Zone updateTo = new Zone(toName, toAddr, recurrentTravel.getTo().getLatitude(), recurrentTravel.getTo()
					.getLongitude(), recurrentTravel.getTo().getRange());
			recurrentTravel.setTo(updateTo);

			if (recurrentTravel.getCommunityIds().isEmpty()) {
				for (Community community : communityRepository.findByUserId(userId)) {
					if (!recurrentTravel.getCommunityIds().contains(community.getId())) {
						recurrentTravel.getCommunityIds().add(community.getId());
					}
				}
			}

			reccurrentTravelRepository.save(recurrentTravel);

			/** create instances of non recurrent travels. **/

			for (int extendDay = 0; extendDay <= CarPoolingUtils.INSTANCES_THRESHOLD; extendDay++) {

				Recurrency recurrency = recurrentTravel.getRecurrency();

				long temp = CarPoolingUtils.adjustNumberOfDaysToWhen(recurrentTravel.getWhen(), extendDay);
				// last instance when to be updated in parent recurrent travel object.
				if (extendDay == CarPoolingUtils.INSTANCES_THRESHOLD) {
					recurrentTravel.setLastInstance(temp);
					reccurrentTravelRepository.save(recurrentTravel);
				}

				// create instance if recurrency applies.
				if (CarPoolingUtils.ifRecurrencyApplies(recurrency, temp)) {
					Travel instance = new Travel();
					// set parent Id.
					instance.setRecurrentId(recurrentTravel.getId());
					instance.setFrom(recurrentTravel.getFrom());
					instance.setTo(recurrentTravel.getTo());
					instance.setWhen(temp);
					instance.setRoute(recurrentTravel.getRoute());
					instance.setUserId(recurrentTravel.getUserId());
					instance.setPlaces(recurrentTravel.getPlaces());
					instance.setIntermediateStops(recurrentTravel.isIntermediateStops());
					instance.setActive(recurrentTravel.isActive());
					instance.setCommunityIds(recurrentTravel.getCommunityIds());

					travelRepository.save(instance);
				}

			}

			// loop on all trip request and check if this new travel matches any
			// of those.
			for (TravelRequest travelRequest : travelRequestRepository.findAllMatchTravelRequest(recurrentTravel)) {

				String travelRequestId = travelRequest.getId();
				String targetUserId = travelRequest.getUserId();
				Map<String, String> data = new HashMap<String, String>();
				data.put("travelRequestId", travelRequestId);

				// get travelId of the instance of recurrent travel.
				Travel instance = travelRepository
						.findOneInstanceOfRecurrTravel(travelRequest, recurrentTravel.getId());

				Notification tripAvailability = new Notification(targetUserId,
						CarPoolingUtils.NOTIFICATION_AVALIABILITY, data, false, instance.getId(),
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

		return recurrentTravel;
	}
	
	@Scheduled(cron = "0 59 23 * * ?")
	public void autoExtendRecurrTravelInstances() throws CarPoolingCustomException {

		logger.info("/**** Extending NonRecurrent instances of RecurrentTravel with window of 30 days.****/ ");

		for (RecurrentTravel recurrentTravel : reccurrentTravelRepository
				.searchTravelsToExtend(CarPoolingUtils.INSTANCES_THRESHOLD)) {

			// calculate the number of days from lastInstance timestamp.
			int pendingDays = CarPoolingUtils.getDaysBetweenDates(recurrentTravel.getLastInstance(),
					System.currentTimeMillis());

			if (pendingDays != 1) {
				logger.warn("encountering abnormal behaviour");
			}

			long temp = recurrentTravel.getLastInstance();
			Recurrency recurrency = recurrentTravel.getRecurrency();

			for (int day = 0; day < pendingDays; day++) {

				temp = CarPoolingUtils.adjustNumberOfDaysToWhen(temp, 1);

				if (CarPoolingUtils.ifRecurrencyApplies(recurrency, temp)) {
					Travel instance = new Travel();
					// set parent Id.
					instance.setRecurrentId(recurrentTravel.getId());
					instance.setFrom(recurrentTravel.getFrom());
					instance.setTo(recurrentTravel.getTo());
					instance.setWhen(temp);
					instance.setRoute(recurrentTravel.getRoute());
					instance.setUserId(recurrentTravel.getUserId());
					instance.setPlaces(recurrentTravel.getPlaces());
					instance.setIntermediateStops(recurrentTravel.isIntermediateStops());
					instance.setActive(recurrentTravel.isActive());
					instance.setCommunityIds(recurrentTravel.getCommunityIds());
					for (RecurrentBooking recurrentBooking : recurrentTravel.getBookings()) {
						Booking book = new Booking();
						book.setRecurrent(true);
						book.setTraveller(recurrentBooking.getTraveller());
						book.setAccepted(recurrentBooking.getAccepted());
						book.setDate(new java.util.Date(System.currentTimeMillis()));
						instance.getBookings().add(book);
					}
					// save new instance.
					travelRepository.save(instance);


				} else {
					logger.info("no instance create as recurrency doesn't apply " + recurrentTravel.getId());
				}
			}

			// update recurrent travel.
			recurrentTravel.setLastInstance(temp);
			reccurrentTravelRepository.save(recurrentTravel);

		}

	}

	public List<TravelRequest> getTravelRequest(String userId) {
		return travelRequestRepository.findByUserId(userId);

	}

	public List<TravelRequest> getMonitoredTravelRequest(String userId) {
		return travelRequestRepository.findMonitoredTravelRequest(userId);
	}

	public void saveTravelRequest(TravelRequest travelRequest) {
		travelRequestRepository.save(travelRequest);
	}

	public List<Travel> getPassengerTrips(String passengerId, int start, int count, Long from, Long to, int order, Boolean boarded, Boolean accepted, String communityId) {
		return travelRepository.findTravelByPassengerId(passengerId, start, count, from, to, order, boarded, accepted, communityId);
	}

	public List<Travel> getDriverTrips(String userId, int start, int count, Long from, Long to, int order) {

//		Page<Travel> travels = travelRepository.findTravelByDriverId(userId, new PageRequest(start, count,
//				Direction.DESC, "route.startime"));
//
//		return travels.getContent();
		return travelRepository.findTravelByDriverId(userId, start, count, from, to, order);
	}

	public List<Travel> searchTravels(TravelRequest travelRequest, String userId) {

		List<Travel> searchTravels = new ArrayList<Travel>();

		searchTravels = travelRepository.searchTravelsExtended(travelRequest);

		if (travelRequest.isMonitored()) {

			String fromName = "";
			String toName = "";
			String fromAddr = "";
			String toAddr = "";
			// make sure if its the logged in user.
			travelRequest.setUserId(userId);

			if (travelRequest.getCommunityIds().isEmpty()) {
				List<String> communityIds = communityRepository.getCommunityIdsForUser(userId);
				travelRequest.setCommunityIds(communityIds);
			}

			if (travelRequest.getFrom().getName() != null && !travelRequest.getFrom().getName().isEmpty()) {
				fromName = travelRequest.getFrom().getName();
			}

			if (travelRequest.getFrom().getAddress() != null && !travelRequest.getFrom().getAddress().isEmpty()) {
				fromAddr = travelRequest.getFrom().getAddress();
			}

			if (travelRequest.getTo().getName() != null && !travelRequest.getTo().getName().isEmpty()) {
				toName = travelRequest.getTo().getName();
			}

			if (travelRequest.getTo().getAddress() != null && !travelRequest.getTo().getAddress().isEmpty()) {
				toAddr = travelRequest.getTo().getAddress();
			}
			// from.
			Zone updateFrom = new Zone(fromName, fromAddr, travelRequest.getFrom().getLatitude(), travelRequest
					.getFrom().getLongitude(), travelRequest.getFrom().getRange());
			travelRequest.setFrom(updateFrom);
			// to
			Zone updateTo = new Zone(toName, toAddr, travelRequest.getTo().getLatitude(), travelRequest.getTo()
					.getLongitude(), travelRequest.getTo().getRange());
			travelRequest.setTo(updateTo);

			travelRequestRepository.save(travelRequest);
		}

		return searchTravels;
	}

	public List<Travel> searchCommunityTravels(String communityId, Long timeInMillies) {

		List<Travel> communityTravels = travelRepository.searchCommunityTravels(communityId, timeInMillies);

		return communityTravels;
	}

	public RecurrentTravel bookRecurrentTravel(String travelId, RecurrentBooking reqBooking, String userId)
			throws CarPoolingCustomException {

		RecurrentTravel travel = reccurrentTravelRepository.findOne(travelId);

		if (travel != null) {

			travel = updateRecurrentTravelBooking(travel, reqBooking, userId);

		} else {
			throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "travel not found.");
		}

		return travel;
	}
	
	/**
	 * Update Booking of Recurrent Travel + Instances.
	 * @param recurrentTravel
	 * @param reqBooking
	 * @param userId
	 * @return
	 * @throws CarPoolingCustomException 
	 */
	private RecurrentTravel updateRecurrentTravelBooking(RecurrentTravel recurrentTravel, RecurrentBooking reqBooking,
			String userId) throws CarPoolingCustomException {

		List<Travel> tranistInstances = travelRepository.findFutureInstanceOfRecurrTravel(recurrentTravel.getId());

		if (tranistInstances != null && !tranistInstances.isEmpty()) {

			// booking instance to replicate.
			Booking instanceBooking = new Booking();
			instanceBooking.setAccepted(0);
			instanceBooking.setTraveller(reqBooking.getTraveller());
			instanceBooking.setRecurrent(true);
			instanceBooking.setDate(new java.util.Date(System.currentTimeMillis()));

			for (Travel instance : tranistInstances) {

				List<Booking> transitStateBooking = instance.getBookings();

				int availability = instance.getPlaces();

				if (transitStateBooking.isEmpty()) {
					// add new booking to instance.
					transitStateBooking.add(instanceBooking);

				} else {

					List<Booking> temp = new ArrayList<Booking>();
					temp.addAll(transitStateBooking);

					boolean updatedNewBooking = false;
					for (Booking uBooking : temp) {

						if (uBooking.getTraveller().getUserId().equalsIgnoreCase(userId)) {
							// 1. check if user is present with recurrent booking -> throw exception [USER ALREADY
							// BOOKED].
							if (uBooking.isRecurrent()) {
								throw new CarPoolingCustomException(HttpStatus.FORBIDDEN.value(),
										"user has already booked.");
							}
							// 2. if user is present with non recurrent booking -> override it.
							if (!uBooking.isRecurrent()) {
								transitStateBooking.remove(uBooking);
								uBooking.setRecurrent(true);
								uBooking.setAccepted(0);
								transitStateBooking.add(uBooking);
								updatedNewBooking = true;
								break;
							}
						} else if (uBooking.getAccepted() != -1) {
							availability--; // 3. if not present check for availability
						}
					}
					if (availability < 1) {
						throw new CarPoolingCustomException(HttpStatus.PRECONDITION_FAILED.value(),
								"travel not bookable.");
					}
					if (!updatedNewBooking) {
						// add new booking to instance.
						transitStateBooking.add(instanceBooking);
					}
				}
			}

			// update recurrent travel.
			reqBooking.getTraveller().setUserId(userId);
			reqBooking.setAccepted(0);
			
			boolean alreadyBooked = false;
			List<RecurrentBooking> tmpList = new ArrayList<RecurrentBooking>();
			if (recurrentTravel.getBookings() != null) tmpList.addAll(recurrentTravel.getBookings());
			for (RecurrentBooking uBooking : tmpList) {
				if (userId.equals(uBooking.getTraveller().getUserId())) {
					alreadyBooked = true;
					// if in the past was rejected, allow for being re-booked again? consider rejecting
					if (uBooking.getAccepted() == -1) {
						uBooking.setAccepted(0);
						reccurrentTravelRepository.save(recurrentTravel);
					}
					break;
				}
			}
			if (!alreadyBooked) {
				recurrentTravel.getBookings().add(reqBooking);
				reccurrentTravelRepository.save(recurrentTravel);
			}

			// update travel instances of recurrent travel.
			travelRepository.save(tranistInstances);

			// create notification.
			String targetUserId = recurrentTravel.getUserId();
			Map<String, String> data = new HashMap<String, String>();
			data.put("senderId", userId);
			User user = userRepository.findOne(userId);
			data.put("senderFullName", user.fullName());
			// always notify with instance of recurrent travel.
			Notification bookingNotification = new Notification(targetUserId, CarPoolingUtils.NOTIFICATION_BOOKING,
					data, false, tranistInstances.get(0).getId(), System.currentTimeMillis());
			notificationRepository.save(bookingNotification);
			// notify via parse.
			try {
				sendPushNotification.sendNotification(targetUserId, bookingNotification);
			} catch (JSONException e) {
				throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
			}

		} else {
			throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					"no instance found for reccurrent travel.");
		}
		

		return recurrentTravel;

	}

	public Travel bookNonRecurrent(String travelId, Booking reqBooking, String userId) throws CarPoolingCustomException {

		Travel travel = travelRepository.findOne(travelId);

		if (travel != null) {

			if (CarPoolingUtils.isValidUser(travel, userId, reqBooking)) {

				if (CarPoolingUtils.havePlaces(travel, reqBooking, userId)) {
					reqBooking.setRecurrent(false);
					reqBooking.getTraveller().setUserId(userId);
					reqBooking.setAccepted(0);
					reqBooking.setDate(new java.util.Date(System.currentTimeMillis()));
					travel.getBookings().add(reqBooking);
					// save travel.
					travelRepository.save(travel);

				} else {
					throw new CarPoolingCustomException(HttpStatus.PRECONDITION_FAILED.value(), "travel not bookable.");
				}
				// create notification.
				String targetUserId = travel.getUserId();
				Map<String, String> data = new HashMap<String, String>();
				data.put("senderId", userId);
				User user = userRepository.findOne(userId);
				data.put("senderFullName", user.fullName());
				Notification bookingNotification = new Notification(targetUserId, CarPoolingUtils.NOTIFICATION_BOOKING,
						data, false, travel.getId(), System.currentTimeMillis());
				notificationRepository.save(bookingNotification);
				// notify via parse.
				try {
					sendPushNotification.sendNotification(targetUserId, bookingNotification);
				} catch (JSONException e) {
					throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
				}

			} else {
				throw new CarPoolingCustomException(HttpStatus.FORBIDDEN.value(), "user has already booked.");
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

	
	public RecurrentTravel acceptRecurrentTrip(String travelId, RecurrentBooking booking, String userId)
			throws CarPoolingCustomException {

		RecurrentTravel travel = reccurrentTravelRepository.findTravelByIdAndDriverId(travelId, userId);
		Travel instanceOfReccTravel = null;

		
		boolean found = false;

		if (travel != null) {
			for (RecurrentBooking book : travel.getBookings()) {
				if (book.equals(booking)) {
					book.setAccepted(booking.getAccepted());
					found = true;
					/**
					 * update instances. (reject instances where recurrentId is travelId and booking.traveller.userId is
					 * recurrBooking->traveller.userId
					 */
					// passengerId.
					String passengerId = booking.getTraveller().getUserId();

					for (Travel instanceRT : travelRepository.findInstanceOfRecurrTravel(travelId)) {
						for (Booking instanceBooking : instanceRT.getBookings()) {
							if (instanceBooking.getTraveller().getUserId().equalsIgnoreCase(passengerId)) {
								if (instanceOfReccTravel == null) {
									instanceOfReccTravel = instanceRT;
								}
								instanceBooking.setAccepted(booking.getAccepted());
								travelRepository.save(instanceRT);
							}
						}
					}
					reccurrentTravelRepository.save(travel);
					break;
				}
			}

			if (found && instanceOfReccTravel != null) {

				String targetUserId = booking.getTraveller().getUserId();
				Map<String, String> data = new HashMap<String, String>();
				data.put("status", "" + booking.getAccepted());
				// always notify with instance of recurrent travel.
				Notification confirmNotification = new Notification(targetUserId, CarPoolingUtils.NOTIFICATION_CONFIRM,
						data, false, instanceOfReccTravel.getId(), System.currentTimeMillis());
				notificationRepository.save(confirmNotification);
				try {
					sendPushNotification.sendNotification(targetUserId, confirmNotification);
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
	
	public Travel acceptNonRecurrentTrip(String travelId, Booking booking, String userId) throws CarPoolingCustomException {

		Travel travel = travelRepository.findTravelByIdAndDriverId(travelId, userId);
		// travelRepository.findOne(travelId);

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
				data.put("status", "" + booking.getAccepted());
				Notification confirmNotification = new Notification(targetUserId, CarPoolingUtils.NOTIFICATION_CONFIRM,
						data, false, travel.getId(), System.currentTimeMillis());
				notificationRepository.save(confirmNotification);
				try {
					sendPushNotification.sendNotification(targetUserId, confirmNotification);
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

		List<Community> filteredCommunities = new ArrayList<Community>();
		List<Community> communities = communityRepository.findByUserId(userId);

		for (Community community : communities) {
			Community temp = new Community(community.getId(), community.getName(), community.getUsers());
			temp.setColor(community.getColor());
			filteredCommunities.add(temp);
		}

		return filteredCommunities;
	}

	public List<Community> readCommunitiesWithDetails(String userId) {
		List<Community> detailedCommunities = new ArrayList<Community>();
		List<Community> communities = communityRepository.findByUserId(userId);

		for (Community community : communities) {

			int nrOfCars = 0;

			for (String id : community.getUsers()) {
				User user = userRepository.findOne(id);
				if (user != null) {
					community.getUserObjs().add(user);
					// incremet car total(if present).
					if (user.getAuto() != null) {
						nrOfCars = nrOfCars + 1;
					}
				}

			}
			community.setCars(nrOfCars);
			detailedCommunities.add(community);
		}

		return detailedCommunities;
	}

	public Community readCommunity(String communityId) {

		Community community = communityRepository.findOne(communityId);

		if (community != null) {

			int nrOfCars = 0;

			for (String id : community.getUsers()) {
				User user = userRepository.findOne(id);
				if (user != null) {
					community.getUserObjs().add(user);
					// incremet car total(if present).
					if (user.getAuto() != null) {
						nrOfCars = nrOfCars + 1;
					}
				}

			}
			community.setCars(nrOfCars);
		}

		return community;
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

	public Integer getMyRatingForPassenger(String userId, String passengerId) throws CarPoolingCustomException {

		Integer rating = null;

		User passenger = userRepository.findOne(passengerId);

		if (passenger != null) {

			GameProfile gameProfile = passenger.getGameProfile();

			if (gameProfile != null && gameProfile.getPassengerRatings().containsKey(userId)) {

				rating = gameProfile.getPassengerRatings().get(userId);

			} else {

				throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
						"passenger rating not found");
			}
		} else {

			throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "passenger not found");
		}

		return rating;
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

	/**
	 * Get My Rating For Driver.
	 * 
	 * @param userId
	 * @param driverId
	 * @return
	 * @throws CarPoolingCustomException
	 */
	public Integer getMyRatingForDriver(String userId, String driverId) throws CarPoolingCustomException {

		Integer rating = null;

		User driver = userRepository.findOne(driverId);

		if (driver != null) {

			GameProfile gameProfile = driver.getGameProfile();

			if (gameProfile != null && gameProfile.getDriverRatings().containsKey(userId)) {

				rating = gameProfile.getDriverRatings().get(userId);

			} else {
				throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "driver rating not found");
			}
		} else {
			throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "driver not found");

		}

		return rating;
	}

	public Map<String, String> updateAutoInfo(String userId, Auto auto) {

		Map<String, String> errorMap = new HashMap<String, String>();

		User driver = userRepository.findOne(userId);

		if (driver != null) {
			if (!auto.getDescription().isEmpty()) { //&& auto.getPosts() > -1
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
			Notification chatNotification = new Notification(targetUserId, CarPoolingUtils.NOTIFICATION_CHAT, data,
					false, travelId, System.currentTimeMillis());
			notificationRepository.save(chatNotification);
			// notify via parse.
			try {
				sendPushNotification.sendNotification(targetUserId, chatNotification);
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

			// msgs are ordered by the insertion at server side, since it can be
			// sent from diff timezones.
			for (Message msg : discussion.getMessages()) {
				if ((msg.getUserId().equalsIgnoreCase(userId) && (msg.getTargetUserId().equalsIgnoreCase(targetUserId)))
						| (msg.getTargetUserId().equalsIgnoreCase(userId) && msg.getUserId().equalsIgnoreCase(
								targetUserId))) { // targetUserId
													// ==
													// userId.(for
													// drivers)
													// or
													// they
													// understand
													// it
													// from
													// bookings
					response.getMessages().add(msg);
				}
			}

		}

		return response;
	}

	/**
	 * read user.
	 * 
	 * @param userId
	 * @return
	 */
	public User readUser(String userId) {
		User user = userRepository.findOne(userId);
		if (user != null) {
			// get count of user offered travels(as driver).
			user.setOfferedTravels(travelRepository.countTravelByDriverId(userId));
			// get count user participated travel(as passenger)
			user.setParticipatedTravels(travelRepository.countTravelByPassengerId(userId));
		}
		return user;
	}

	/**
	 * read notifications.
	 * 
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
	 * read notifications of travel
	 * @param userId
	 * @param travelId
	 * @return
	 */
	public List<Notification> readNotificationsOfTravelId(String travelId) {

		List<Notification> notifications = notificationRepository.findByTravelId(travelId);

		return notifications;
	}

	/**
	 * mark notification as read.
	 * 
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
	 * 
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
	 * 
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
	
	/**
	 * Delete Travel.
	 * @param travelId
	 * @param userId
	 * @return
	 * @throws CarPoolingCustomException
	 */
	public Map<String, String> deleteTravel(String travelId, String userId) throws CarPoolingCustomException {

		Map<String, String> errorMap = new HashMap<String, String>();

		Travel travel = travelRepository.findTravelByIdAndDriverId(travelId, userId);

		if (travel != null) {
			// check if travel can be deleted.
			if (CarPoolingUtils.isPossibleToDeleteTravel(travel)) {
				travelRepository.delete(travel);
			} else {
				throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "cannot delete travel");
			}

		} else {
			throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "travel not found");
		}

		return errorMap;
	}

	/**
	 * Delete Recurrent Travel
	 * @param recurrentTravelId
	 * @param userId
	 * @return
	 */
	public Map<String, String> deleteRecurrentTravel(String recurrentTravelId, String userId)
			throws CarPoolingCustomException {

		Map<String, String> errorMap = new HashMap<String, String>();

		RecurrentTravel recurrentTravel = reccurrentTravelRepository.findTravelByIdAndDriverId(recurrentTravelId,
				userId);

		if (recurrentTravel != null) {

			// get all future instances of recurrent travel.
			List<Travel> futureInstances = travelRepository.findFutureInstanceOfRecurrTravel(recurrentTravelId);

			// check if recurrent travel can be deleted.
			if (CarPoolingUtils.isPossibleToDeleteRecurrTravel(recurrentTravel, futureInstances)) {

				// delete future instances.
				if (!futureInstances.isEmpty()) {
					travelRepository.delete(futureInstances);
				}

				// delete recurrent travel;
				reccurrentTravelRepository.delete(recurrentTravel);
			} else {
				throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "cannot delete recurrent travel");	
			}

		} else {
			throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "recurrent travel not found");
		}

		return errorMap;
	}

	@Scheduled(cron = "0 0 0/1 * * ?")
	public void autoSendEvaluationNotification() throws CarPoolingCustomException {
		logger.info("/**** Firing Rating Notifications****/ ");

		// search for travels that are completed at this time.
		for (Travel travel : travelRepository.searchCompletedTravels(System.currentTimeMillis())) {

			// check if travel is already rated, look in notification.
			List<Notification> travelRatingNotifications = notificationRepository.findByTravelIdAndNotificationType(
					travel.getId(), CarPoolingUtils.NOTIFICATION_RATING);

			if (travelRatingNotifications != null && travelRatingNotifications.size() > 0) {
				continue;
			} else {
				try {

					String travelId = travel.getId();
					Long timestamp = System.currentTimeMillis();

					// create notifications for passengers.
					Map<String, String> dataPassengerNotification = new HashMap<String, String>();
					dataPassengerNotification
							.put("message", "Si prega di valutare il conducente di viaggio completato");
					dataPassengerNotification.put("travelId", travelId);

					boolean nofityDriver = false;

					for (Booking booking : travel.getBookings()) {
						if (booking.getAccepted() == 1 && booking.getBoarded() == 1) {
							nofityDriver = true;
							String passengerId = booking.getTraveller().getUserId();
							Notification passengerRatingNotification = new Notification(passengerId,
									CarPoolingUtils.NOTIFICATION_RATING, dataPassengerNotification, false, travelId,
									timestamp);
							notificationRepository.save(passengerRatingNotification);
							sendPushNotification.sendNotification(passengerId, passengerRatingNotification);
						}
					}

					if (nofityDriver) {

						// create notification for driver only if there is
						// atleast a passenger accepted.
						String driverId = travel.getUserId();
						Map<String, String> dataDriverNotification = new HashMap<String, String>();
						dataDriverNotification.put("message",
								"Si prega di valutare i passeggeri del suo viaggio completato");
						dataDriverNotification.put("travelId", travelId);

						Notification driverRatingNotification = new Notification(driverId,
								CarPoolingUtils.NOTIFICATION_RATING, dataDriverNotification, false, travelId, timestamp);
						notificationRepository.save(driverRatingNotification);
						sendPushNotification.sendNotification(driverId, driverRatingNotification);

					}
				} catch (JSONException e) {
					throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
				}
			}
		}
	}

	
//	public boolean performUpdateRecurrBooking(RecurrentTravel travel, RecurrentBooking reqBooking, String userId) {
//
//		boolean bookable = false;
//		int capacity = travel.getPlaces();
//
//		for (RecurrentBooking booking : travel.getBookings()) {
//			if (booking.getAccepted() != -1) {
//				capacity--;
//			}
//		}
//
//		// check that there are places in the bookings of all the Travel objects
//		// with this recurrencyId
//		if (capacity > 0) {
//			List<Travel> instances = travelRepository.findFutureInstanceOfRecurrTravel(travel.getId());
//
//			for (Travel instance : instances) {
//				int seats = instance.getPlaces();
//				for (Booking booking : instance.getBookings()) {
//					if (booking.getAccepted() != -1) {
//						seats--;
//					}
//				}
//				// user must not have booking of himself with recurrent.
//				if (seats < 1) {
//					bookable = false;
//					break;
//				}
//			}
//		}
//		return bookable;
//	}

	public Booking updateBoarding(String tripId, String userId, int boarding) throws CarPoolingCustomException {

		Travel travel = travelRepository.findOne(tripId);

		Booking foundBooking = null;

		for (Booking booking : travel.getBookings()) {
			if (booking.getTraveller().getUserId().equalsIgnoreCase(userId)) {
				booking.setBoarded(boarding);
				foundBooking = booking;
				break;
			}
		}

		if (foundBooking != null) {
			travelRepository.save(travel);
			return foundBooking;
		} else {
			throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "booking not found");
		}

	}

	/**
	 * @param tripId
	 * @return
	 * @throws CarPoolingCustomException 
	 */
	public Recurrency getRecurrency(String tripId) throws CarPoolingCustomException {
		RecurrentTravel rt = reccurrentTravelRepository.findOne(tripId);
		if (rt != null) return rt.getRecurrency();
		throw new CarPoolingCustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "recurrent travel not found");
	}


}