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
import it.smartcommunitylab.carpooling.mongo.repos.CommunityRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRepository;
import it.smartcommunitylab.carpooling.test.TestConfig;

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
public class TestRepository {

	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private TravelRepository travelRepository;

	@Before
	public void init() {

	}

	@Test
	public void testCommunityRepo() {
		for (Community community : communityRepository.findByUserId("52")) {
			System.out.println(community.getId());
		}
	}
	
	@Test
	public void testTravelRepo() {
		for (Travel travel : travelRepository.findTravelByPassengerId("54")) {
			System.out.println(travel.getId());
		}
	}

}
