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

package it.smartcommunitylab.carpooling.test.v2.manager;

import java.io.IOException;
import java.io.InputStream;
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

import it.smartcommunitylab.carpooling.exceptions.CarPoolingCustomException;
import it.smartcommunitylab.carpooling.managers.CarPoolingManager;
import it.smartcommunitylab.carpooling.model.Community;
import it.smartcommunitylab.carpooling.model.RecurrentTravel;
import it.smartcommunitylab.carpooling.model.Travel;
import it.smartcommunitylab.carpooling.model.TravelRequest;
import it.smartcommunitylab.carpooling.model.User;
import it.smartcommunitylab.carpooling.mongo.repos.CommunityRepository;
import it.smartcommunitylab.carpooling.mongo.repos.RecurrentTravelRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRequestRepository;
import it.smartcommunitylab.carpooling.mongo.repos.UserRepository;
import it.smartcommunitylab.carpooling.test.TestConfig;
import it.smartcommunitylab.carpooling.utils.CarPoolingUtils;

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
	private RecurrentTravelRepository recurrentTravelRepository;
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private TravelRepository travelRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CarPoolingManager carPoolingManager;
	

	private ObjectMapper mapper = new ObjectMapper();

	@After
	public void after() {
		travelRequestRepository.deleteAll();
		recurrentTravelRepository.deleteAll();
		communityRepository.deleteAll();
		travelRepository.deleteAll();
		travelRequestRepository.deleteAll();
		userRepository.deleteAll();

	}

	@Before
	public void before() {

		travelRequestRepository.deleteAll();
		recurrentTravelRepository.deleteAll();
		communityRepository.deleteAll();
		travelRepository.deleteAll();
		travelRequestRepository.deleteAll();
		userRepository.deleteAll();
		
		InputStream userJson = Thread.currentThread().getContextClassLoader().getResourceAsStream("users-v2.json");
		InputStream communityJson = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("community-v2.json");

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
	public void testV2CreateTravelWithMatchingReq() throws JsonProcessingException, IOException, CarPoolingCustomException {
		
		InputStream reqTravel = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("travelReqs-v2.json");
		JsonNode travelReqNode = mapper.readTree(reqTravel);
		ArrayNode travelReqArray = (ArrayNode) travelReqNode;
		
		for (JsonNode node: travelReqArray) {
			TravelRequest travelReq = mapper.convertValue(node, TravelRequest.class);
			// +hour, -hour.
			travelRequestRepository.save(travelReq);
		}
		
		InputStream recurrTravel = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("travelsRecurr-v2.json");
		JsonNode travelRootNode = mapper.readTree(recurrTravel);
		ArrayNode travelArrayNode = (ArrayNode) travelRootNode;
		
		for (JsonNode node : travelArrayNode) {
			RecurrentTravel refRecurrentTravel = mapper.convertValue(node, RecurrentTravel.class);

			carPoolingManager.saveRecurrentTravel(refRecurrentTravel, "52");

		}
		
		

	}
	
	@Test
	public void TestV2CreateExtendInstances() throws JsonProcessingException, IOException, CarPoolingCustomException {

		InputStream reqTravel = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("travelsRecurr-v2.json");
		JsonNode travelReqNode = mapper.readTree(reqTravel);
		ArrayNode travelReqArray = (ArrayNode) travelReqNode;
		
		// create recurrent travel of now.
		for (JsonNode node: travelReqArray) {
			RecurrentTravel reccurrTravel = mapper.convertValue(node, RecurrentTravel.class);
			// +hour, -hour.
			reccurrTravel.setWhen(CarPoolingUtils.adjustNumberOfDaysToWhen(System.currentTimeMillis(), -1));
			carPoolingManager.saveRecurrentTravel(reccurrTravel, "52");
		}
		
		Assert.assertEquals(travelRepository.count(), 9);
		
		// call extension
		carPoolingManager.autoExtendRecurrTravelInstances();
		
		Assert.assertEquals(travelRepository.count(), 10);

	}

	@Test
	public void testV2SearchTravel() throws JsonProcessingException, IOException {

		// create non recurrent travel.
		InputStream nonRecurrTravelJson = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("nonRecurrTravel-v2.json");
		JsonNode nonRecurrTravelNode = mapper.readTree(nonRecurrTravelJson);
		ArrayNode nonRecurrTArrayNode = (ArrayNode) nonRecurrTravelNode;
		Travel nonRecurrTravel = mapper.convertValue(nonRecurrTArrayNode.get(0), Travel.class);
		travelRepository.save(nonRecurrTravel);
		
		// construct ref Travel Req from json file.
		InputStream travelReqJson = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("single-travelReq-v2.json");
		JsonNode rootNode = mapper.readTree(travelReqJson);
		ArrayNode arrayNode = (ArrayNode) rootNode;
		TravelRequest travelRequest = mapper.convertValue(arrayNode.get(0), TravelRequest.class);

		List<Travel> travels = carPoolingManager.searchTravels(travelRequest, "53"); // search after one month.

		Assert.assertFalse(travels.isEmpty());

		// for (Travel travel : travels) {
		// System.out.println(travel.getId());
		// }

	}
	
	//
	// @Test
	// public void testRecurrentTravelBooking() throws JsonProcessingException,
	// IOException, ParseException, CarPoolingCustomException {
	//
	// /**
	// * travel with 4 places, has two recurrent bookings. (since one is
	// rejected, see recurrentTravel.json)
	// * user(53) request for non-recurrent booking and allowed.
	// * user(53) request for recurrnt booking on different date and allowed
	// * user(52) request for reccruent booking and allowed.
	// * user(70) request for reccurrent booking and disallowed.
	// * driver rejects above bookings.
	// * user(70) request for reccurrent booking and allowed + accepted by
	// driver.
	// */
	//
	// travelRepository.deleteAll();
	//
	// // construct ref Travel from json file.
	// InputStream jsonlFile = Thread.currentThread().getContextClassLoader()
	// .getResourceAsStream("recurrentTravel.json");
	// JsonNode rootNode = mapper.readTree(jsonlFile);
	// ArrayNode arrayNode = (ArrayNode) rootNode;
	//
	// for (JsonNode node : arrayNode) {
	// Travel travel = mapper.convertValue(node, Travel.class);
	// travelRepository.save(travel);
	// }
	//
	// InputStream travelReqJson =
	// Thread.currentThread().getContextClassLoader()
	// .getResourceAsStream("travelReq.json");
	// JsonNode travelReqrootNode = mapper.readTree(travelReqJson);
	// ArrayNode travelReqArrayNode = (ArrayNode) travelReqrootNode;
	// TravelRequest travelRequest =
	// mapper.convertValue(travelReqArrayNode.get(0), TravelRequest.class);
	//
	// List<Travel> travels = travelManager.searchTravels(travelRequest, "53");
	//
	// Assert.assertEquals(travels.get(0).getBookings().size(), 3); // since one
	// booking is rejected.
	//
	// Booking nonRecurr1 = new Booking();
	// nonRecurr1.setRecurrent(false);
	// nonRecurr1.setDate(CarPoolingUtils.dateFormat.parse("2015-09-29"));
	// nonRecurr1.setTraveller(new Traveller("53", "User Reccurrent",
	// "User Reccurrent", null));
	//
	// Travel travel = travelManager.bookTrip("560263eed1f1f802c2a83book",
	// nonRecurr1, "53");
	//
	// Assert.assertEquals(travel.getBookings().size(), 4);
	//
	// // second booking for same user.
	// Booking nonRecurr2 = new Booking();
	// nonRecurr2.setRecurrent(false);
	// nonRecurr2.setDate(CarPoolingUtils.dateFormat.parse("2015-09-30"));
	// nonRecurr2.setTraveller(new Traveller("53", "User Reccurrent",
	// "User Reccurrent", null));
	//
	// travel = travelManager.bookTrip("560263eed1f1f802c2a83book", nonRecurr2,
	// "53");
	//
	// Assert.assertEquals(travel.getBookings().size(), 5);
	//
	// Booking recurrBooking = new Booking();
	// recurrBooking.setRecurrent(true);
	// recurrBooking.setTraveller(new Traveller("52", "User Reccurrent",
	// "User Reccurrent", null));
	//
	// travel = travelManager.bookTrip("560263eed1f1f802c2a83book",
	// recurrBooking, "52");
	//
	// Assert.assertEquals(travel.getBookings().size(), 6);
	//
	// Booking recurrBooking2 = new Booking();
	// recurrBooking2.setRecurrent(true);
	// recurrBooking2.setTraveller(new Traveller("70",
	// "Blocked User Reccurrent", "Blocked User Reccurrent", null));
	//
	// travel = travelManager.bookTrip("560263eed1f1f802c2a83book",
	// recurrBooking2, "70");
	//
	// Assert.assertEquals(travel.getBookings().size(), 6);
	//
	// // reject first two non recurrent bookings.
	// nonRecurr1.setAccepted(-1);
	// nonRecurr2.setAccepted(-1);
	// travel = travelManager.acceptTrip("560263eed1f1f802c2a83book",
	// nonRecurr1, "54");
	// travel = travelManager.acceptTrip("560263eed1f1f802c2a83book",
	// nonRecurr2, "54");
	//
	// recurrBooking.setAccepted(-1);
	// travel = travelManager.acceptTrip("560263eed1f1f802c2a83book",
	// recurrBooking, "54");
	//
	// travel = travelManager.bookTrip("560263eed1f1f802c2a83book",
	// recurrBooking2, "70");
	//
	// Assert.assertEquals(travel.getBookings().size(), 7);
	//
	// recurrBooking2.setAccepted(1);
	// travel = travelManager.acceptTrip("560263eed1f1f802c2a83book",
	// recurrBooking2, "54");
	//
	// boolean accepted = false;
	// for (Booking booking : travel.getBookings()) {
	// if (booking.getTraveller().getUserId().equalsIgnoreCase("70") &&
	// booking.getAccepted() == 1) {
	// accepted = true;
	// }
	// }
	//
	// Assert.assertTrue(accepted);
	// }
	//
	// @Test
	// public void testTravelRequestMatching() throws JsonProcessingException,
	// IOException, CarPoolingCustomException {
	//
	// InputStream jsonTravelRequestFile =
	// Thread.currentThread().getContextClassLoader()
	// .getResourceAsStream("match-travel-request.json");
	// JsonNode rootNodeTravelReq = mapper.readTree(jsonTravelRequestFile);
	// ArrayNode arrayNodeTravelReqs = (ArrayNode) rootNodeTravelReq;
	// for (JsonNode node : arrayNodeTravelReqs) {
	// TravelRequest refTravelRequest = mapper.convertValue(node,
	// TravelRequest.class);
	// travelRequestRepository.save(refTravelRequest);
	//
	// }
	//
	// // create travel with ref Travel from json file.
	// InputStream jsonlFile =
	// Thread.currentThread().getContextClassLoader().getResourceAsStream("match-travel.json");
	// JsonNode rootNode = mapper.readTree(jsonlFile);
	// ArrayNode arrayNode = (ArrayNode) rootNode;
	// for (JsonNode node : arrayNode) {
	// Travel refTravel = mapper.convertValue(node, Travel.class);
	// travelManager.saveTravel(refTravel, "53");
	//
	// }
	// }

}
