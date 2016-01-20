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

package it.smartcommunitylab.carpooling.test.managers;

import it.smartcommunitylab.carpooling.exceptions.CarPoolingCustomException;
import it.smartcommunitylab.carpooling.managers.CarPoolingManager;
import it.smartcommunitylab.carpooling.model.Booking;
import it.smartcommunitylab.carpooling.model.Community;
import it.smartcommunitylab.carpooling.model.RecurrentBooking;
import it.smartcommunitylab.carpooling.model.RecurrentTravel;
import it.smartcommunitylab.carpooling.model.Travel;
import it.smartcommunitylab.carpooling.model.TravelProfile;
import it.smartcommunitylab.carpooling.model.TravelRequest;
import it.smartcommunitylab.carpooling.model.Traveller;
import it.smartcommunitylab.carpooling.model.User;
import it.smartcommunitylab.carpooling.mongo.repos.CommunityRepository;
import it.smartcommunitylab.carpooling.mongo.repos.RecurrentTravelRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRepository;
import it.smartcommunitylab.carpooling.mongo.repos.UserRepository;
import it.smartcommunitylab.carpooling.test.TestConfig;
import it.smartcommunitylab.carpooling.utils.CarPoolingUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@org.junit.Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
public class TestUser {

	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private TravelRepository travelRepository;
	@Autowired
	private RecurrentTravelRepository recurrentTravelRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CarPoolingManager travelManager;

	private ObjectMapper mapper = new ObjectMapper();

	@After
	public void after() {
		travelRepository.deleteAll();
		userRepository.deleteAll();
		communityRepository.deleteAll();

	}

	@Before
	public void before() {
		InputStream travelJson = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("travel-booked.json");
		InputStream userJson = Thread.currentThread().getContextClassLoader().getResourceAsStream("users.json");
		InputStream communityJson = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("community.json");

		try {
			JsonNode travelRootNode = mapper.readTree(travelJson);
			ArrayNode travelArrayNode = (ArrayNode) travelRootNode;

			for (JsonNode node : travelArrayNode) {
				Travel refTravel = mapper.convertValue(node, Travel.class);
				travelRepository.save(refTravel);

			}

			JsonNode userRootNode = mapper.readTree(userJson);
			ArrayNode usersArrayNode = (ArrayNode) userRootNode;

			for (JsonNode uNode : usersArrayNode) {
				User refUser = mapper.convertValue(uNode, User.class);
				userRepository.save(refUser);

			}

			JsonNode communityRootNode = mapper.readTree(communityJson);
			ArrayNode commArrayNode = (ArrayNode) communityRootNode;

			for (JsonNode cNode : commArrayNode) {
				Community comm = mapper.convertValue(cNode, Community.class);
				communityRepository.save(comm);
			}

		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void testUserTransitionReccToRecc() throws JsonProcessingException, IOException {
		/**
		 * User("52") with Recc Booking exist.
		 * User try to make another Recc booking.
		 * forbid user.
		 */

		travelRepository.deleteAll();
		recurrentTravelRepository.deleteAll();

		// construct ref Travel from json file.
		InputStream jsonlFile = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("recurrentTravel.json");
		JsonNode rootNode = mapper.readTree(jsonlFile);
		ArrayNode arrayNode = (ArrayNode) rootNode;

		for (JsonNode node : arrayNode) {
			RecurrentTravel travel = mapper.convertValue(node, RecurrentTravel.class);
			travel.setWhen(System.currentTimeMillis());
			try {

				travelManager.saveRecurrentTravel(travel, "54");

			} catch (CarPoolingCustomException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

//		InputStream travelReqJson = Thread.currentThread().getContextClassLoader()
//				.getResourceAsStream("travelReq.json");
//		JsonNode travelReqrootNode = mapper.readTree(travelReqJson);
//		ArrayNode travelReqArrayNode = (ArrayNode) travelReqrootNode;
//		TravelRequest travelRequest = mapper.convertValue(travelReqArrayNode.get(0), TravelRequest.class);
//		travelRequest.setWhen(CarPoolingUtils.adjustNumberOfDaysToWhen(System.currentTimeMillis(), 1));
//		travelRequest.setUserId("52");

		RecurrentBooking reccBooking = new RecurrentBooking();
		reccBooking.setTraveller(new Traveller("52", "User Reccurrent", "User Reccurrent", null));

		RecurrentTravel travel;
		try {
			travel = travelManager.bookRecurrentTravel("560263eed1f1f802c2a83book", reccBooking, "52");
			Assert.assertEquals(travel.getBookings().size(), 1);
		} catch (CarPoolingCustomException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			travel = travelManager.bookRecurrentTravel("560263eed1f1f802c2a83book", reccBooking, "52");
		} catch (CarPoolingCustomException cpe) {
			Assert.assertEquals(cpe.getBody().getErrorCode(), HttpStatus.FORBIDDEN.value());
		}

	}

	@Test
	public void testUserTransitionReccToNonRecc() throws JsonProcessingException, IOException {
		/**
		 * User("52") with Recc booking exist.
		 * User to make Non-Recc booking.
		 * User not allowed.
		 * Previous booking remains.
		 */
	
		travelRepository.deleteAll();
		recurrentTravelRepository.deleteAll();

		// construct ref Travel from json file.
		InputStream jsonlFile = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("recurrentTravel.json");
		JsonNode rootNode = mapper.readTree(jsonlFile);
		ArrayNode arrayNode = (ArrayNode) rootNode;

		for (JsonNode node : arrayNode) {
			RecurrentTravel travel = mapper.convertValue(node, RecurrentTravel.class);
			travel.setWhen(System.currentTimeMillis());
			try {

				travelManager.saveRecurrentTravel(travel, "54");

			} catch (CarPoolingCustomException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		InputStream travelReqJson = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("travelReq.json");
		JsonNode travelReqrootNode = mapper.readTree(travelReqJson);
		ArrayNode travelReqArrayNode = (ArrayNode) travelReqrootNode;
		TravelRequest travelRequest = mapper.convertValue(travelReqArrayNode.get(0), TravelRequest.class);
		travelRequest.setWhen(CarPoolingUtils.adjustNumberOfDaysToWhen(System.currentTimeMillis(), 1));
		travelRequest.setUserId("52");

		RecurrentBooking reccBooking = new RecurrentBooking();
		reccBooking.setTraveller(new Traveller("52", "User Reccurrent", "User Reccurrent", null));

		RecurrentTravel travel;
		try {
			travel = travelManager.bookRecurrentTravel("560263eed1f1f802c2a83book", reccBooking, "52");
			Assert.assertEquals(travel.getBookings().size(), 1);
		} catch (CarPoolingCustomException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Booking nonReccBooking = new Booking();
		nonReccBooking.setRecurrent(false);
		nonReccBooking.setDate(new java.util.Date(System.currentTimeMillis()));
		nonReccBooking.setTraveller(new Traveller("52", "User Reccurrent", "User Reccurrent", null));

		List<Travel> userTravels = travelRepository.findTravelByPassengerId("52", 0, 20, null, null, -1, false, null);
		List<Booking> reccBookingsForUser = CarPoolingUtils.getAllReccBookingForUserTravels(userTravels, "52");
		List<Booking> nonReccBookingsForUser = CarPoolingUtils.getAllNonReccBookingForUserTravels(userTravels, "52");

		try {
			Travel travelNR = travelManager.bookNonRecurrent(userTravels.get(0).getId(), nonReccBooking, "52");
		} catch (CarPoolingCustomException cpe) {
			Assert.assertEquals(cpe.getBody().getErrorCode(), HttpStatus.FORBIDDEN.value());
		}
		
	}

	@Test
	public void testUserTransitionNonReccToRecc() throws JsonProcessingException, IOException, ParseException {
		/**
		 * User("52") with non-recc booking exist.
		 * User try to make recc booking.
		 * User non recc booking overwritten with recc one.
		 */
		
		Booking booking = null;
		RecurrentTravel recurrTravel = null;
		Travel nonRecurrTravel = null;

		travelRepository.deleteAll();
		recurrentTravelRepository.deleteAll();

		// construct ref Travel from json file.
		InputStream jsonlFile = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("recurrentTravel.json");
		JsonNode rootNode = mapper.readTree(jsonlFile);
		ArrayNode arrayNode = (ArrayNode) rootNode;

		for (JsonNode node : arrayNode) {
			RecurrentTravel travel = mapper.convertValue(node, RecurrentTravel.class);
			travel.setWhen(System.currentTimeMillis());
			try {

				travelManager.saveRecurrentTravel(travel, "54");
				recurrTravel = travel;

			} catch (CarPoolingCustomException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		InputStream travelReqJson = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("travelReq.json");
		JsonNode travelReqrootNode = mapper.readTree(travelReqJson);
		ArrayNode travelReqArrayNode = (ArrayNode) travelReqrootNode;
		TravelRequest travelRequest = mapper.convertValue(travelReqArrayNode.get(0), TravelRequest.class);
		travelRequest.setWhen(CarPoolingUtils.adjustNumberOfDaysToWhen(System.currentTimeMillis(), 1));

		List<Travel> travels = travelManager.searchTravels(travelRequest, "52");

		try {
			Booking nonRecurr1 = new Booking();
			nonRecurr1.setRecurrent(false);
			nonRecurr1.setDate(CarPoolingUtils.dateFormat.parse("2015-09-29"));
			nonRecurr1.setTraveller(new Traveller("52", "Non Reccurrent User", "Non Reccurrent User", null));

			Travel travel = travelManager.bookNonRecurrent(travels.get(0).getId(), nonRecurr1, "52");
			Assert.assertEquals(travel.getBookings().size(), 1);
			nonRecurrTravel = travel;

			Assert.assertEquals(travel.getBookings().size(), 1);
			Assert.assertFalse(travel.getBookings().get(0).isRecurrent());

			RecurrentBooking recurrBooking = new RecurrentBooking();
			recurrBooking.setTraveller(new Traveller("52", "User Reccurrent", "User Reccurrent", null));

			RecurrentTravel travelR = travelManager.bookRecurrentTravel(recurrTravel.getId(), recurrBooking, "52");
			Assert.assertEquals(travel.getBookings().size(), 1);

			Travel travelUpdated = travelRepository.findOne(travel.getId());

			Assert.assertEquals(travelUpdated.getBookings().size(), 1);
			Assert.assertTrue(travelUpdated.getBookings().get(0).isRecurrent());

		} catch (CarPoolingCustomException cpe) {
			System.err.println(cpe.getMessage());
		}

	}

	@Test
	public void testUserTransitionNonReccToNonRecc() throws JsonProcessingException, IOException {
		/**
		 * User("53") with non recc booking exist.
		 * User try to make another non recc booking on different date.
		 * User is allowed.
		 * User try to make another non recc booking on existing booked date for that user.
		 * User not allowed.
		 */
	
		travelRepository.deleteAll();
		recurrentTravelRepository.deleteAll();

		// construct ref Travel from json file.
		InputStream jsonlFile = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("recurrentTravel.json");
		JsonNode rootNode = mapper.readTree(jsonlFile);
		ArrayNode arrayNode = (ArrayNode) rootNode;

		for (JsonNode node : arrayNode) {
			RecurrentTravel travel = mapper.convertValue(node, RecurrentTravel.class);
			travel.setWhen(System.currentTimeMillis());
			try {

				travelManager.saveRecurrentTravel(travel, "54");

			} catch (CarPoolingCustomException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		InputStream travelReqJson = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("travelReq.json");
		JsonNode travelReqrootNode = mapper.readTree(travelReqJson);
		ArrayNode travelReqArrayNode = (ArrayNode) travelReqrootNode;
		TravelRequest travelRequest = mapper.convertValue(travelReqArrayNode.get(0), TravelRequest.class);
		travelRequest.setWhen(CarPoolingUtils.adjustNumberOfDaysToWhen(System.currentTimeMillis(), 1));

		List<Travel> travels = travelManager.searchTravels(travelRequest, "53");

		try {
			Booking nonRecurr1 = new Booking();
			nonRecurr1.setRecurrent(false);
			nonRecurr1.setDate(CarPoolingUtils.dateFormat.parse("2015-09-29"));
			nonRecurr1.setTraveller(new Traveller("52", "Non Reccurrent User", "Non Reccurrent User", null));

			Travel travel = travelManager.bookNonRecurrent(travels.get(0).getId(), nonRecurr1, "53");
			Assert.assertEquals(travel.getBookings().size(), 1);

			travelRequest.setWhen(CarPoolingUtils.adjustNumberOfDaysToWhen(System.currentTimeMillis(), 2));

			travels = travelManager.searchTravels(travelRequest, "53");
			travel = travelManager.bookNonRecurrent(travels.get(0).getId(), nonRecurr1, "53");

			Assert.assertEquals(travel.getBookings().size(), 1);

			travel = travelManager.bookNonRecurrent(travels.get(0).getId(), nonRecurr1, "53");

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // available date.
		catch (CarPoolingCustomException cpe) {
			Assert.assertEquals(cpe.getBody().getErrorCode(), HttpStatus.FORBIDDEN.value());
		}

	}

	@Test
	public void testSelfRatingForbidden() {
		/**
		 * user ("52"), rate driver("54") with rating 5.
		 * driver("54"), try to selft rate, forbidden.
		 * driver("54"), score remains 5.
		 * 
		 */
		User driver = userRepository.findOne("54");
		Assert.assertEquals(driver.getGameProfile().getDriverRating(), 0, 0);
		travelManager.rateDriver("52", "54", 5);
		Assert.assertEquals(driver.getGameProfile().getDriverRating(), 5, 5);
		travelManager.rateDriver("54", "54", 5);
		Assert.assertEquals(driver.getGameProfile().getDriverRating(), 5, 5);

	}

	@Test
	public void testRatingDriver() {
		/**
		 * driver("54") rate user ("52") with rating 10.
		 * driver("54") rate user("52") with rating 5.
		 * previous rating overwritten.
		 * user("52") try to self rate to 10, forbidden.
		 * user("52") score remains 5.
		 */
		User passenger = userRepository.findOne("52");
		Assert.assertEquals(passenger.getGameProfile().getDriverRating(), 0, 0);
		travelManager.ratePassenger("54", "52", 10);
		Assert.assertEquals(passenger.getGameProfile().getDriverRating(), 10, 10);
		travelManager.ratePassenger("54", "52", 5);
		Assert.assertEquals(passenger.getGameProfile().getDriverRating(), 5, 5);
		travelManager.ratePassenger("52", "52", 10);
		Assert.assertEquals(passenger.getGameProfile().getDriverRating(), 5, 5);

	}

	@Test
	public void testRatingPassenger() {
		/**
		 * user("52") rate user("53") to 5.
		 * user("53") rate user("52") to 5.
		 * driver("54") rate both users("52","53") to 0.
		 * both users rating drops to 2.5(50%).
		 * 
		 */
		User passenger1 = userRepository.findOne("52");
		User passenger2 = userRepository.findOne("53");
		Assert.assertEquals(passenger1.getGameProfile().getDriverRating(), 0, 0);
		Assert.assertEquals(passenger2.getGameProfile().getDriverRating(), 0, 0);
		travelManager.ratePassenger("52", "53", 5);
		travelManager.ratePassenger("53", "52", 5);
		Assert.assertEquals(passenger1.getGameProfile().getDriverRating(), 5, 5);
		Assert.assertEquals(passenger2.getGameProfile().getDriverRating(), 5, 5);
		travelManager.ratePassenger("54", "53", 0);
		travelManager.ratePassenger("54", "52", 0);
		Assert.assertEquals(passenger1.getGameProfile().getDriverRating(), 2.5, 2.5);
		Assert.assertEquals(passenger2.getGameProfile().getDriverRating(), 2.5, 2.5);

	}

	@Test
	public void testSaveReadProfile() throws JsonProcessingException, IOException, CarPoolingCustomException {
		/**
		 * user("52") read profile, empty.
		 * user("52") save a travel profile and read it back.
		 */
		InputStream travelProfileJson = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("travel-profile.json");
		JsonNode travelProfileNode = mapper.readTree(travelProfileJson);
		ArrayNode travelProfileArrayNode = (ArrayNode) travelProfileNode;
		TravelProfile refTravelProfile = mapper.convertValue(travelProfileArrayNode.get(0), TravelProfile.class);

//		TravelProfile travelProfile = travelManager.readTravelProfile("52");

//		Assert.assertTrue(travelProfile.getRoutes().isEmpty());

		travelManager.saveTravelProfile(refTravelProfile, "52");

		TravelProfile travelProfile = travelManager.readTravelProfile("52");

		Assert.assertFalse(travelProfile.getRoutes().isEmpty());

	}

	@Test
	public void readCommunities() {
		/**
		 * reference user(52,53,54,70) is present in two communities.
		 * reference user(60,65) is present in one community.
		 */
		Assert.assertEquals(travelManager.readCommunities("52").size(), 2);
		Assert.assertEquals(travelManager.readCommunities("53").size(), 2);
		Assert.assertEquals(travelManager.readCommunities("54").size(), 2);
		Assert.assertEquals(travelManager.readCommunities("70").size(), 2);

		Assert.assertEquals(travelManager.readCommunities("60").size(), 1);
		Assert.assertEquals(travelManager.readCommunities("65").size(), 1);

	}

}
