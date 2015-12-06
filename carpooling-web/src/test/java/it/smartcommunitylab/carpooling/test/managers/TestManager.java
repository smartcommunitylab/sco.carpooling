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
import it.smartcommunitylab.carpooling.model.TravelRequest;
import it.smartcommunitylab.carpooling.model.Traveller;
import it.smartcommunitylab.carpooling.model.User;
import it.smartcommunitylab.carpooling.mongo.repos.CommunityRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRequestRepository;
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

/**
 * 
 * @author nawazk
 *
 */
@org.junit.Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
public class TestManager {

	@Autowired
	private TravelRequestRepository travelRequestRepository;
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
		travelRequestRepository.deleteAll();
		communityRepository.deleteAll();
		travelRepository.deleteAll();
		userRepository.deleteAll();

	}

	@Before
	public void before() {

		travelRequestRepository.deleteAll();
		communityRepository.deleteAll();
		travelRepository.deleteAll();
		userRepository.deleteAll();

		InputStream travelJson = Thread.currentThread().getContextClassLoader().getResourceAsStream("travel.json");
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
	public void testSearchTravel() throws JsonProcessingException, IOException {

		// construct ref Travel from json file.
		InputStream travelReqJson = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("travelReq.json");
		JsonNode rootNode = mapper.readTree(travelReqJson);
		ArrayNode arrayNode = (ArrayNode) rootNode;
		TravelRequest travelRequest = mapper.convertValue(arrayNode.get(0), TravelRequest.class);

		List<String> commIdsForUser = communityRepository.getCommunityIdsForUser("52");

		List<Travel> travels = travelRepository.searchTravels(commIdsForUser, travelRequest);

		Assert.assertFalse(travels.isEmpty());

//		for (Travel travel : travels) {
//			System.out.println(travel.getId());
//		}

	}

	@Test
	public void TestSearchWithCommunityCriteria() throws JsonProcessingException, IOException {

		InputStream travelReqJson = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("travelReq.json");
		JsonNode rootNode = mapper.readTree(travelReqJson);
		ArrayNode arrayNode = (ArrayNode) rootNode;
		TravelRequest travelRequest = mapper.convertValue(arrayNode.get(0), TravelRequest.class);

		List<String> commIdsForUser = communityRepository.getCommunityIdsForUser("65");

		List<Travel> travels = travelRepository.searchTravels(commIdsForUser, travelRequest);

		Assert.assertEquals(travels.size(), 1);
		// driver has same community as of user.
		String driverId = travels.get(0).getUserId();

		boolean found = false;
		for (String communityId : commIdsForUser) {
			Community community = communityRepository.findOne(communityId);
			if (community.getUsers().contains(driverId)) {
				found = true;
				break;
			}
		}
		Assert.assertTrue(found);

	}

	@Test
	public void testRecurrentTravelBooking() throws JsonProcessingException, IOException, ParseException, CarPoolingCustomException {

		/**
		 * travel with 4 places, has two recurrent bookings. (since one is rejected, see recurrentTravel.json)
		 * user(53) request for non-recurrent booking and allowed. 
		 * user(53) request for recurrnt booking on different date and allowed
		 * user(52) request for reccruent booking and allowed.
		 * user(70) request for reccurrent booking and disallowed.
		 * driver rejects above bookings.
		 * user(70) request for reccurrent booking and allowed + accepted by driver.
		 */

		travelRepository.deleteAll();

		// construct ref Travel from json file.
		InputStream jsonlFile = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("recurrentTravel.json");
		JsonNode rootNode = mapper.readTree(jsonlFile);
		ArrayNode arrayNode = (ArrayNode) rootNode;

		for (JsonNode node : arrayNode) {
			Travel travel = mapper.convertValue(node, Travel.class);
			travelRepository.save(travel);
		}

		InputStream travelReqJson = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("travelReq.json");
		JsonNode travelReqrootNode = mapper.readTree(travelReqJson);
		ArrayNode travelReqArrayNode = (ArrayNode) travelReqrootNode;
		TravelRequest travelRequest = mapper.convertValue(travelReqArrayNode.get(0), TravelRequest.class);

		List<Travel> travels = travelManager.searchTravels(travelRequest, "53");

		Assert.assertEquals(travels.get(0).getBookings().size(), 3); // since one booking is rejected.

		Booking nonRecurr1 = new Booking();
		nonRecurr1.setRecurrent(false);
		nonRecurr1.setDate(CarPoolingUtils.dateFormat.parse("2015-09-29"));
		nonRecurr1.setTraveller(new Traveller("53", "User Reccurrent", "User Reccurrent", null));

		Travel travel = travelManager.bookTrip("560263eed1f1f802c2a83book", nonRecurr1, "53");

		Assert.assertEquals(travel.getBookings().size(), 4);

		// second booking for same user.
		Booking nonRecurr2 = new Booking();
		nonRecurr2.setRecurrent(false);
		nonRecurr2.setDate(CarPoolingUtils.dateFormat.parse("2015-09-30"));
		nonRecurr2.setTraveller(new Traveller("53", "User Reccurrent", "User Reccurrent", null));

		travel = travelManager.bookTrip("560263eed1f1f802c2a83book", nonRecurr2, "53");

		Assert.assertEquals(travel.getBookings().size(), 5);

		Booking recurrBooking = new Booking();
		recurrBooking.setRecurrent(true);
		recurrBooking.setTraveller(new Traveller("52", "User Reccurrent", "User Reccurrent", null));

		travel = travelManager.bookTrip("560263eed1f1f802c2a83book", recurrBooking, "52");

		Assert.assertEquals(travel.getBookings().size(), 6);

		Booking recurrBooking2 = new Booking();
		recurrBooking2.setRecurrent(true);
		recurrBooking2.setTraveller(new Traveller("70", "Blocked User Reccurrent", "Blocked User Reccurrent", null));

		travel = travelManager.bookTrip("560263eed1f1f802c2a83book", recurrBooking2, "70");

		Assert.assertEquals(travel.getBookings().size(), 6);

		// reject first two non recurrent bookings.
		nonRecurr1.setAccepted(-1);
		nonRecurr2.setAccepted(-1);
		travel = travelManager.acceptTrip("560263eed1f1f802c2a83book", nonRecurr1, "54");
		travel = travelManager.acceptTrip("560263eed1f1f802c2a83book", nonRecurr2, "54");

		recurrBooking.setAccepted(-1);
		travel = travelManager.acceptTrip("560263eed1f1f802c2a83book", recurrBooking, "54");

		travel = travelManager.bookTrip("560263eed1f1f802c2a83book", recurrBooking2, "70");

		Assert.assertEquals(travel.getBookings().size(), 7);

		recurrBooking2.setAccepted(1);
		travel = travelManager.acceptTrip("560263eed1f1f802c2a83book", recurrBooking2, "54");

		boolean accepted = false;
		for (Booking booking : travel.getBookings()) {
			if (booking.getTraveller().getUserId().equalsIgnoreCase("70") && booking.getAccepted() == 1) {
				accepted = true;
			}
		}

		Assert.assertTrue(accepted);
	}

}
