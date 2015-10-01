package it.smartcommunitylab.carpooling.test.managers;

import it.smartcommunitylab.carpooling.managers.CarPoolingManager;
import it.smartcommunitylab.carpooling.model.Booking;
import it.smartcommunitylab.carpooling.model.Community;
import it.smartcommunitylab.carpooling.model.Travel;
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

//@org.junit.Ignore
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
	public void testUserTransitionReccToRecc() {
		/**
		 * User("52") with Recc Booking exist.
		 * User try to make another Recc booking.
		 * Previous booking overwritten.
		 */
		Booking reccBooking = new Booking();
		reccBooking.setRecurrent(true);
		reccBooking.setTraveller(new Traveller("52", "User Reccurrent", "User Reccurrent", null));

		List<Travel> userTravels = travelRepository.findTravelByPassengerId("52");
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

			List<Travel> userTravels = travelRepository.findTravelByPassengerId("52");
			List<Booking> reccBookingsForUser = CarPoolingUtils.getAllReccBookingForUserTravels(userTravels, "52");
			List<Booking> nonReccBookingsForUser = CarPoolingUtils
					.getAllNonReccBookingForUserTravels(userTravels, "52");

			Travel travel = travelManager.bookTrip("560263eed1f1f802c2a83book", nonReccBooking, "52");

			List<Travel> postBookingTravels = travelRepository.findTravelByPassengerId("52");
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

	}

	@Test
	public void testUserTransitionNonReccToRecc() {
		/**
		 * User("53") with non-recc booking exist.
		 * User try to make recc booking.
		 * User non recc booking overwritten with recc one.
		 */
		Booking reccBooking = new Booking();
		reccBooking.setRecurrent(true);
		reccBooking.setTraveller(new Traveller("53", "User Non recurrent", "User Non recurrent", null));

		List<Travel> userTravels = travelRepository.findTravelByPassengerId("53");
		List<Booking> reccBookingsForUser = CarPoolingUtils.getAllReccBookingForUserTravels(userTravels, "53");
		List<Booking> nonReccBookingsForUser = CarPoolingUtils.getAllNonReccBookingForUserTravels(userTravels, "53");

		Travel travel = travelManager.bookTrip("560263eed1f1f802c2a83book", reccBooking, "53");

		List<Travel> postBookingTravels = travelRepository.findTravelByPassengerId("53");
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

			List<Travel> userTravels = travelRepository.findTravelByPassengerId("53");
			List<Booking> reccBookingsForUser = CarPoolingUtils.getAllReccBookingForUserTravels(userTravels, "53");
			List<Booking> nonReccBookingsForUser = CarPoolingUtils
					.getAllNonReccBookingForUserTravels(userTravels, "53");

			Assert.assertEquals(nonReccBookingsForUser.size(), 1);
			Assert.assertEquals(reccBookingsForUser.size(), 0);

			Travel travel = travelManager.bookTrip("560263eed1f1f802c2a83book", nonRecc1, "53");

			List<Travel> postBookingTravels = travelRepository.findTravelByPassengerId("53");
			List<Booking> postBookingReccBookingsForUser = CarPoolingUtils.getAllReccBookingForUserTravels(
					postBookingTravels, "53");
			List<Booking> postBookingNonReccBookingsForUser = CarPoolingUtils.getAllNonReccBookingForUserTravels(
					postBookingTravels, "53");

			Assert.assertEquals(postBookingReccBookingsForUser.size(), 0);
			Assert.assertEquals(postBookingNonReccBookingsForUser.size(), 2);

			travel = travelManager.bookTrip("560263eed1f1f802c2a83book", nonRecc1, "53");

			List<Travel> postBooking2Travels = travelRepository.findTravelByPassengerId("53");
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

	}

}
