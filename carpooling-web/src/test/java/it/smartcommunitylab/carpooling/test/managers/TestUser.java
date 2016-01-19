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
import it.smartcommunitylab.carpooling.model.Travel;
import it.smartcommunitylab.carpooling.model.TravelProfile;
import it.smartcommunitylab.carpooling.model.Traveller;
import it.smartcommunitylab.carpooling.model.User;
import it.smartcommunitylab.carpooling.mongo.repos.CommunityRepository;
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
	public void testUserTransitionReccToRecc() throws CarPoolingCustomException {
		/**
		 * User("52") with Recc Booking exist.
		 * User try to make another Recc booking.
		 * Previous booking overwritten.
		 */
		Booking reccBooking = new Booking();
		reccBooking.setRecurrent(true);
		reccBooking.setTraveller(new Traveller("52", "User Reccurrent", "User Reccurrent", null));

		List<Travel> userTravels = travelRepository.findTravelByPassengerId("52", 0, 20);
		Assert.assertFalse(userTravels.isEmpty());
		Travel existingTravelForUser = userTravels.get(0);

		Travel travel = travelManager.bookTrip("560263eed1f1f802c2a83book", reccBooking, "52");

		Assert.assertEquals(existingTravelForUser.getBookings().size(), travel.getBookings().size());

	}

	@Test
	public void testUserTransitionReccToNonRecc() {
		/**
		 * User("52") with Recc booking exist.
		 * User to make Non-Recc booking.
		 * User not allowed.
		 * Previous booking remains.
		 */
		try {

			Booking nonReccBooking = new Booking();
			nonReccBooking.setDate(CarPoolingUtils.dateFormat.parse("2015-09-29"));
			nonReccBooking.setRecurrent(false);
			nonReccBooking.setTraveller(new Traveller("52", "User Reccurrent", "User Reccurrent", null));

			List<Travel> userTravels = travelRepository.findTravelByPassengerId("52", 0, 20);
			List<Booking> reccBookingsForUser = CarPoolingUtils.getAllReccBookingForUserTravels(userTravels, "52");
			List<Booking> nonReccBookingsForUser = CarPoolingUtils
					.getAllNonReccBookingForUserTravels(userTravels, "52");

			Travel travel = travelManager.bookTrip("560263eed1f1f802c2a83book", nonReccBooking, "52");

			List<Travel> postBookingTravels = travelRepository.findTravelByPassengerId("52", 0, 20);
			List<Booking> postBookingReccBookingsForUser = CarPoolingUtils.getAllReccBookingForUserTravels(
					postBookingTravels, "52");
			List<Booking> postBookingnonReccBookingsForUser = CarPoolingUtils.getAllNonReccBookingForUserTravels(
					postBookingTravels, "52");

			Assert.assertEquals(reccBookingsForUser.size(), postBookingReccBookingsForUser.size());
			Assert.assertEquals(nonReccBookingsForUser.size(), 0);
			Assert.assertEquals(postBookingnonReccBookingsForUser.size(), 0);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // booking avaialble for this date.
		catch (CarPoolingCustomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void testUserTransitionNonReccToRecc() throws CarPoolingCustomException {
		/**
		 * User("53") with non-recc booking exist.
		 * User try to make recc booking.
		 * User non recc booking overwritten with recc one.
		 */
		Booking reccBooking = new Booking();
		reccBooking.setRecurrent(true);
		reccBooking.setTraveller(new Traveller("53", "User Non recurrent", "User Non recurrent", null));

		List<Travel> userTravels = travelRepository.findTravelByPassengerId("53", 0, 20);
		List<Booking> reccBookingsForUser = CarPoolingUtils.getAllReccBookingForUserTravels(userTravels, "53");
		List<Booking> nonReccBookingsForUser = CarPoolingUtils.getAllNonReccBookingForUserTravels(userTravels, "53");

		Travel travel = travelManager.bookTrip("560263eed1f1f802c2a83book", reccBooking, "53");

		List<Travel> postBookingTravels = travelRepository.findTravelByPassengerId("53", 0, 20);
		List<Booking> postBookingReccBookingsForUser = CarPoolingUtils.getAllReccBookingForUserTravels(
				postBookingTravels, "53");
		List<Booking> postBookingNonReccBookingsForUser = CarPoolingUtils.getAllNonReccBookingForUserTravels(
				postBookingTravels, "53");

		Assert.assertEquals(nonReccBookingsForUser.size(), 1);
		Assert.assertEquals(reccBookingsForUser.size(), 0);
		Assert.assertEquals(postBookingReccBookingsForUser.size(), 1);
		Assert.assertEquals(postBookingNonReccBookingsForUser.size(), 0);

	}

	@Test
	public void testUserTransitionNonReccToNonRecc() {
		/**
		 * User("53") with non recc booking exist.
		 * User try to make another non recc booking on different date.
		 * User is allowed.
		 * User try to make another non recc booking on existing booked date for that user.
		 * User not allowed.
		 */
		try {

			Booking nonRecc1 = new Booking();
			nonRecc1.setRecurrent(false);
			nonRecc1.setDate(CarPoolingUtils.dateFormat.parse("2015-09-29"));
			nonRecc1.setTraveller(new Traveller("53", "User Non recurrent", "User Non recurrent", null));

			List<Travel> userTravels = travelRepository.findTravelByPassengerId("53", 0, 20);
			List<Booking> reccBookingsForUser = CarPoolingUtils.getAllReccBookingForUserTravels(userTravels, "53");
			List<Booking> nonReccBookingsForUser = CarPoolingUtils
					.getAllNonReccBookingForUserTravels(userTravels, "53");

			Assert.assertEquals(nonReccBookingsForUser.size(), 1);
			Assert.assertEquals(reccBookingsForUser.size(), 0);

			Travel travel = travelManager.bookTrip("560263eed1f1f802c2a83book", nonRecc1, "53");

			List<Travel> postBookingTravels = travelRepository.findTravelByPassengerId("53", 0, 20);
			List<Booking> postBookingReccBookingsForUser = CarPoolingUtils.getAllReccBookingForUserTravels(
					postBookingTravels, "53");
			List<Booking> postBookingNonReccBookingsForUser = CarPoolingUtils.getAllNonReccBookingForUserTravels(
					postBookingTravels, "53");

			Assert.assertEquals(postBookingReccBookingsForUser.size(), 0);
			Assert.assertEquals(postBookingNonReccBookingsForUser.size(), 2);

			travel = travelManager.bookTrip("560263eed1f1f802c2a83book", nonRecc1, "53");

			List<Travel> postBooking2Travels = travelRepository.findTravelByPassengerId("53", 0, 20);
			List<Booking> postBooking2ReccBookingsForUser = CarPoolingUtils.getAllReccBookingForUserTravels(
					postBookingTravels, "53");
			List<Booking> postBooking2NonReccBookingsForUser = CarPoolingUtils.getAllNonReccBookingForUserTravels(
					postBookingTravels, "53");

			Assert.assertEquals(postBooking2ReccBookingsForUser.size(), 0);
			Assert.assertEquals(postBooking2NonReccBookingsForUser.size(), 2);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // available date.
		catch (CarPoolingCustomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

		TravelProfile travelProfile = travelManager.readTravelProfile("52");

		Assert.assertTrue(travelProfile.getRoutes().isEmpty());

		travelManager.saveTravelProfile(refTravelProfile, "52");

		travelProfile = travelManager.readTravelProfile("52");

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
