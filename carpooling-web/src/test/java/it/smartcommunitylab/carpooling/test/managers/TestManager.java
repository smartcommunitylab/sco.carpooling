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
import it.smartcommunitylab.carpooling.model.TravelRequest;
import it.smartcommunitylab.carpooling.model.Traveller;
import it.smartcommunitylab.carpooling.model.User;
import it.smartcommunitylab.carpooling.mongo.repos.CommunityRepository;
import it.smartcommunitylab.carpooling.mongo.repos.RecurrentTravelRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRequestRepository;
import it.smartcommunitylab.carpooling.mongo.repos.UserRepository;
import it.smartcommunitylab.carpooling.test.TestConfig;
import it.smartcommunitylab.carpooling.utils.CarPoolingUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
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
	private RecurrentTravelRepository recurrentTravelRepo;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CarPoolingManager travelManager;

	private ObjectMapper mapper = new ObjectMapper();

	@After
	public void after() {
		travelRequestRepository.deleteAll();
		recurrentTravelRepo.deleteAll();
		communityRepository.deleteAll();
		travelRepository.deleteAll();
		travelRequestRepository.deleteAll();
		userRepository.deleteAll();

	}

	@Before
	public void before() {

		travelRequestRepository.deleteAll();
		communityRepository.deleteAll();
		travelRepository.deleteAll();
		recurrentTravelRepo.deleteAll();
		travelRequestRepository.deleteAll();
		userRepository.deleteAll();

		InputStream userJson = Thread.currentThread().getContextClassLoader().getResourceAsStream("users.json");
		InputStream communityJson = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("community.json");
		try {
		
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
	public void TestSearchWithAndWithoutCommunityCriteria() throws JsonProcessingException, IOException {

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
		JsonNode rNodeTReq = mapper.readTree(travelReqJson);
		ArrayNode arrNodeTReq = (ArrayNode) rNodeTReq;
		TravelRequest travelRequest = mapper.convertValue(arrNodeTReq.get(0), TravelRequest.class);
		travelRequest.setWhen(CarPoolingUtils.adjustNumberOfDaysToWhen(System.currentTimeMillis(), 30));

		List<Travel> travels = travelRepository.searchTravels(travelRequest);

		Assert.assertEquals(travels.size(), 1);
		// driver has same community as of user.
		String driverId = travels.get(0).getUserId();

		boolean found = false;
		for (String communityId : travelRequest.getCommunityIds()) {
			Community community = communityRepository.findOne(communityId);
			if (community.getUsers().contains(driverId)) {
				found = true;
				break;
			}
		}
		Assert.assertTrue(found);
		
		travelRequest.getCommunityIds().clear();
		
		List<String> community = new ArrayList<String>();
		community.add("INEXISTENT");
		
		travelRequest.setCommunityIds(community);
		
		travels = travelRepository.searchTravels(travelRequest);
		
		Assert.assertEquals(travels.size(), 0);

	}

	@Test
	public void testRecurrentTravelBooking() throws JsonProcessingException, IOException, ParseException {

		/**
		 * exist recurrent travel with 4 places.
		 * user(53) request for non-recurrent booking and allowed. 
		 * 	-> instance of recurr travel updated.
		 * user(53) request for non recurrent booking on different date and now allowed.
		 * user(53) request for recurrent booking -> allowed and overridden (non recurrent booking).
		 * user(52) request for reccruent booking and allowed.
		 * user(55) request for reccruent booking and allowed.
		 * user(56) request for reccruent booking and allowed.
		 * user(70) request for reccurrent booking and disallowed.
		 * driver rejects above bookings.
		 * user(70) request for reccurrent booking and allowed + accepted by driver.
		 */

		travelRepository.deleteAll();
		recurrentTravelRepo.deleteAll();

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

		Booking nonRecurr1 = new Booking();
		nonRecurr1.setRecurrent(false);
		nonRecurr1.setDate(CarPoolingUtils.dateFormat.parse("2015-09-29"));
		nonRecurr1.setTraveller(new Traveller("53", "Non Reccurrent User", "Non Reccurrent User", null));

		try {
			Travel travel = travelManager.bookNonRecurrent(travels.get(0).getId(), nonRecurr1, "53");
			Assert.assertEquals(travel.getBookings().size(), 1);
		} catch (CarPoolingCustomException cpe) {
		}

		Booking nonRecurr2 = new Booking();
		nonRecurr2.setRecurrent(false);
		nonRecurr2.setDate(CarPoolingUtils.dateFormat.parse("2015-09-30"));
		nonRecurr2.setTraveller(new Traveller("53", "Non Reccurrent User", "Non Reccurrent User", null));

		try {
			Travel travel = travelManager.bookNonRecurrent(travels.get(0).getId(), nonRecurr2, "53");
			Assert.assertEquals(travel.getBookings().size(), 1);
		} catch (CarPoolingCustomException cpe) {
			System.err.println(cpe.getMessage());
		}

		// user 53 ask for recurrent booking and allowed -> allowed overridden.
		RecurrentBooking recurrBooking = new RecurrentBooking();
		recurrBooking.setTraveller(new Traveller("53", "User Reccurrent", "User Reccurrent", null));

		try {

			RecurrentTravel travel = travelManager.bookRecurrentTravel("560263eed1f1f802c2a83book", recurrBooking,
					"53");
			System.err.println(travel.getWhen());

			Assert.assertEquals(travel.getBookings().size(), 1);

			// user 52 ask for recurrent booking, allowed.
			recurrBooking.setTraveller(new Traveller("52", "User Reccurrent", "User Reccurrent", null));
			travel = travelManager.bookRecurrentTravel("560263eed1f1f802c2a83book", recurrBooking, "52");

			Assert.assertEquals(travel.getBookings().size(), 2);

			recurrBooking.setTraveller(new Traveller("55", "User Recurrent", "User Recurrent", null));
			travel = travelManager.bookRecurrentTravel("560263eed1f1f802c2a83book", recurrBooking, "55");

			Assert.assertEquals(travel.getBookings().size(), 3);

			recurrBooking.setTraveller(new Traveller("56", "User Recurrent", "User Recurrent", null));
			travel = travelManager.bookRecurrentTravel("560263eed1f1f802c2a83book", recurrBooking, "56");

			Assert.assertEquals(travel.getBookings().size(), 4);

		} catch (CarPoolingCustomException cpe) {
			System.err.println(cpe.getMessage());
		}

		try {
			recurrBooking.setTraveller(new Traveller("70", "Blocked Userd", "Blocked User", null));
			RecurrentTravel travel = travelManager.bookRecurrentTravel("560263eed1f1f802c2a83book", recurrBooking,
					"70");
		} catch (CarPoolingCustomException cpe) {
			Assert.assertEquals(cpe.getBody().getErrorCode(), 412);
		}

//		reject recurrent bookings for user 56.
		recurrBooking.setTraveller(new Traveller("56", "User Recurrent", "User Recurrent", null));
		recurrBooking.setAccepted(-1);
		try {
			RecurrentTravel travel = travelManager.acceptRecurrentTrip("560263eed1f1f802c2a83book", recurrBooking,
					"54");
//			Assert.assertEquals(travel.getBookings().size(), 3);
			
			boolean rejected = false;
			for (RecurrentBooking booking : travel.getBookings()) {
				if (booking.getTraveller().getUserId().equalsIgnoreCase("56") && booking.getAccepted() == -1) {
					rejected = true;
				}
			}
			
			Assert.assertTrue(rejected);

			recurrBooking.setTraveller(new Traveller("70", "Blocked User", "Blocked User", null));
			travel = travelManager.bookRecurrentTravel("560263eed1f1f802c2a83book", recurrBooking, "70");

			Assert.assertEquals(travel.getBookings().size(), 5);

			recurrBooking.setTraveller(new Traveller("70", "User Recurrent Accepted", "User Recurrent Accepted", null));
			recurrBooking.setAccepted(1);

			travel = travelManager.acceptRecurrentTrip("560263eed1f1f802c2a83book", recurrBooking, "54");

			boolean accepted = false;
			for (RecurrentBooking booking : travel.getBookings()) {
				if (booking.getTraveller().getUserId().equalsIgnoreCase("70") && booking.getAccepted() == 1) {
					accepted = true;
				}
			}

			Assert.assertTrue(accepted);

		} catch (CarPoolingCustomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void testTravelRequestMatching() throws JsonProcessingException, IOException, CarPoolingCustomException {

		InputStream jsonTravelRequestFile = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("match-travel-request.json");
		JsonNode rootNodeTravelReq = mapper.readTree(jsonTravelRequestFile);
		ArrayNode arrayNodeTravelReqs = (ArrayNode) rootNodeTravelReq;
		for (JsonNode node : arrayNodeTravelReqs) {
			TravelRequest refTravelRequest = mapper.convertValue(node, TravelRequest.class);
			travelRequestRepository.save(refTravelRequest);

		}

		// create travel with ref Travel from json file.
		InputStream jsonlFile = Thread.currentThread().getContextClassLoader().getResourceAsStream("match-travel.json");
		JsonNode rootNode = mapper.readTree(jsonlFile);
		ArrayNode arrayNode = (ArrayNode) rootNode;
		for (JsonNode node : arrayNode) {
			Travel refTravel = mapper.convertValue(node, Travel.class);
			travelManager.saveTravel(refTravel, "53");

		}
	}

}
