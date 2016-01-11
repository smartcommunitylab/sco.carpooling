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

package it.smartcommunitylab.carpooling.test.data;

import it.smartcommunitylab.carpooling.model.Community;
import it.smartcommunitylab.carpooling.model.Travel;
import it.smartcommunitylab.carpooling.model.TravelRequest;
import it.smartcommunitylab.carpooling.model.Zone;
import it.smartcommunitylab.carpooling.mongo.repos.CommunityRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRepository;
import it.smartcommunitylab.carpooling.test.TestConfig;
import it.smartcommunitylab.carpooling.utils.CarPoolingUtils;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 
 * @author nawazk
 *
 */
//@org.junit.Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
public class TestRepository {

	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private TravelRepository travelRepository;

	private ObjectMapper mapper = new ObjectMapper();

	private static Travel refTravel;

	
	@Before
	public void init() throws JsonProcessingException, IOException {
		InputStream jsonlFile = Thread.currentThread().getContextClassLoader().getResourceAsStream("reccNonReccTravel.json");
		JsonNode rootNode = mapper.readTree(jsonlFile);
		ArrayNode arrayNode = (ArrayNode) rootNode;
		for (JsonNode node : arrayNode) {
			refTravel = mapper.convertValue(node, Travel.class);
			// save.
			travelRepository.save(refTravel);
		}
	}
	
	@After
	public void after() {
		travelRepository.deleteAll();

	}

//	@Test
	public void testCommunityRepo() {
		for (Community community : communityRepository.findByUserId("52")) {
			System.out.println(community.getId());
		}
	}

	@Test
	public void testTravelRepoByPassengerId() {
		for (Travel travel : travelRepository.findTravelByPassengerId("52")) {
			System.out.println(travel.getId());
		}
		// pagination.
		for (Travel travel : travelRepository.findTravelByPassengerId("52",3,1)) {
			System.out.println(travel.getId());
		}
	}
	
	@Test
	public void testTravelRepoByDriverId() {

		Page<Travel> travels = travelRepository.findTravelByDriverId("53", new PageRequest(0, 20, Direction.DESC,
				"route.startime"));
		
		for (Travel travel : travels.getContent()) {
			System.out.println(travel.getId());
		}
	}
	
	@Test
	public void testCompletedTravels() {
		
		for (Travel travel: travelRepository.searchCompletedTravels(System.currentTimeMillis())) {
			System.out.println(travel.getId());
		}
	}
	
//	@Test
	public void testTravelRepoById() {
		Travel travel = travelRepository.findOne("560263eed1f1f802c2a83efg");
		System.out.println(travel.getId());
	}


//	@Test
	public void testCommunityMatchInSearchTravel() {

		List<String> communityIds = communityRepository.getCommunityIdsForUser("52");

		for (Travel travel : travelRepository.getAllMatchedCommunityTravels(communityIds)) {
			System.out.println(travel.getId());
		}
	}

	@Test
	public void testZoneMatchInSearchTravel() throws JsonProcessingException, IOException {

		//construct ref Travel from json file.
		InputStream jsonlFile = Thread.currentThread().getContextClassLoader().getResourceAsStream("travel.json");
		JsonNode rootNode = mapper.readTree(jsonlFile);
		ArrayNode arrayNode = (ArrayNode) rootNode;
		for (JsonNode node : arrayNode) {
			refTravel = mapper.convertValue(node, Travel.class);
			// save.
			travelRepository.save(refTravel);
		}
		TravelRequest travelRequest = new TravelRequest();
		//valid 'From' points (witin 1km.)
//		travelRequest.setFrom(new Zone("Castello Buon Consiglio", "Castello Buon Consiglio", 46.071386, 11.127772, 0.4));
//		travelRequest.setFrom(new Zone("Via Archangelo Rezzi, Centro", "Via Archangelo Rezzi, Centro", 46.067250, 11.120252, 0.8));
		travelRequest.setFrom(new Zone("Via Fiume", "Via Fiume", 46.065487, 11.131346, 1));

//		invalid 'From' points(outside 1km.)
//		travelRequest.setFrom(new Zone("Spalliera Cereria", "Spalliera Cereria",46.078088, 11.125556, 1)); //1.2km is reachable.
//		travelRequest.setFrom(new Zone("Parco di Gocciadoro", "Parco di Gocciadoro", 46.054712, 11.136401, 1)); //1.8km is reachable.
//		travelRequest.setFrom(new Zone("Via Solteri", "Via Solteri", 46.087617, 11.121586, 1)); //2.3km is reachable.

		//valid 'To' points within 1km.
//		travelRequest.setTo(new Zone("Teatro Portland", "Teatro Portland", 46.070009, 11.112011, 0.34));
//		travelRequest.setTo(new Zone("Ponte di San Giorgio", "Ponte di San Giorgio", 46.077552, 11.115269, 0.81)); 
		travelRequest.setTo(new Zone("Muse", "Muse", 46.063266, 11.113062, 1));

//		invalid 'To' points outside 1km.
//		travelRequest.setTo(new Zone("Angolo dei 33", "Angolo dei 33", 46.07548,11.105595, 1)); //1.2 is reachable.
//		travelRequest.setTo(new Zone("Hotel Vela", "Hotel Vela", 46.082421, 11.102012, 1)); // 2km is reachable.
//		travelRequest.setTo(new Zone("Hotel Sporting Trento", "Hotel Sporting Trento", 46.051344, 11.111677, 1)); //2.1km is reachable.

//		for (Travel travel : travelRepository.getAllMatchedZoneTravels(travelRequest)) {
//			System.out.println(travel.getId());
//		}
		
		Assert.assertFalse(travelRepository.getAllMatchedZoneTravels(travelRequest).isEmpty());
	}
	
	
	@Test
	public void testTimeMatchTravel() throws JsonProcessingException, IOException {

		// // construct ref Travel from json file.
		InputStream jsonlFile = Thread.currentThread().getContextClassLoader().getResourceAsStream("travel.json");
		JsonNode rootNode = mapper.readTree(jsonlFile);
		ArrayNode arrayNode = (ArrayNode) rootNode;
		for (JsonNode node : arrayNode) {
			refTravel = mapper.convertValue(node, Travel.class);
			// save.
			travelRepository.save(refTravel);
		}
		
		TravelRequest travelRequest = new TravelRequest();
		//valid 'From' points (witin 1km.)
		//travelRequest.setFrom(new Zone("Castello Buon Consiglio", "Castello Buon Consiglio", 46.071386, 11.127772, 1));
		//travelRequest.setFrom(new Zone("Via Archangelo Rezzi, Centro", "Via Archangelo Rezzi, Centro", 46.067250, 11.120252, 1));
		travelRequest.setFrom(new Zone("Via Fiume", "Via Fiume", 46.065487, 11.131346, 1));

//		invalid 'From' points(outside 1km.)
//		travelRequest.setFrom(new Zone("Spalliera Cereria", "Spalliera Cereria",46.078088, 11.125556, 1));
//		travelRequest.setFrom(new Zone("Parco di Gocciadoro", "Parco di Gocciadoro", 46.054712, 11.136401, 1));
//		travelRequest.setFrom(new Zone("Via Solteri", "Via Solteri", 46.087617, 11.121586, 1));

		//valid 'To' points within 1km.
		//travelRequest.setTo(new Zone("Teatro Portland", "Teatro Portland", 46.070009, 11.112011, 1));
		//travelRequest.setTo(new Zone("Ponte di San Giorgio", "Ponte di San Giorgio", 46.077552, 11.115269, 1)); 
		travelRequest.setTo(new Zone("Muse", "Muse", 46.063266, 11.113062, 1));

//		invalid 'To' points outside 1km.
//		travelRequest.setTo(new Zone("Angolo dei 33", "Angolo dei 33", 46.07548,11.105595, 1));
//		travelRequest.setTo(new Zone("Hotel Vela", "Hotel Vela", 46.082421, 11.102012, 1));
//		travelRequest.setTo(new Zone("Hotel Sporting Trento", "Hotel Sporting Trento", 46.051344, 11.111677, 1));
		
//		travelRequest.setTimestamp(1443427200000L); //10am (Sept 28)
//		travelRequest.setTimestamp(1443412800000L); //6am (Sept 28)
//		travelRequest.setTimestamp(1443420000000L); //8am (Sept 28)
//		travelRequest.setTimestamp(1443427320000L); //10:02am (Sept 28)
		travelRequest.setWhen(1443425400000L); //09:30am (Sept 28)
		
//		for (Travel travel : travelRepository.getAllMatchedTimeTravels(travelRequest)) {
//			System.out.println(travel.getId());
//		}
		
		Assert.assertFalse(travelRepository.getAllMatchedTimeTravels(travelRequest).isEmpty());
	}
	
	@Test
	public void testSameDayUtilityMethod() {
		Assert.assertTrue(CarPoolingUtils.isOnSameDay(1443425400000L, 1443472200000L));
	}

}
