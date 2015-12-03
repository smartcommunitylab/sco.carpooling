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

import it.smartcommunitylab.carpooling.managers.CarPoolingManager;
import it.smartcommunitylab.carpooling.model.Notification;
import it.smartcommunitylab.carpooling.mongo.repos.NotificationRepository;
import it.smartcommunitylab.carpooling.test.TestConfig;

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

@org.junit.Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
public class TestNotifications {

	@Autowired
	NotificationRepository notificationRepository;

	@Autowired
	CarPoolingManager carPoolingManager;

	private ObjectMapper mapper = new ObjectMapper();

	@After
	public void after() {
		notificationRepository.deleteAll();
	}

	@Before
	public void before() {

		notificationRepository.deleteAll();

		InputStream notificationJson = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("notification.json");

		try {
			JsonNode notificationsRootNode = mapper.readTree(notificationJson);
			ArrayNode notificationsArrayNode = (ArrayNode) notificationsRootNode;

			for (JsonNode node : notificationsArrayNode) {
				Notification notification = mapper.convertValue(node, Notification.class);
				notificationRepository.save(notification);

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
	public void testPagination() {

		List<Notification> notificationsPage1 = carPoolingManager.readNotifications("53", 0, 3);
		List<Notification> notificationsPage2 = carPoolingManager.readNotifications("53", 1, 3);
		List<Notification> notificationsPage3 = carPoolingManager.readNotifications("53", 2, 3);
		List<Notification> notificationsPage4 = carPoolingManager.readNotifications("53", 3, 3);

		System.out.println("PAGE#1");
		for (Notification notificationPage1 : notificationsPage1) {
			System.out.println(notificationPage1.getTravelId());

		}
		System.out.println("PAGE#2");
		for (Notification notificationPage2 : notificationsPage2) {
			System.out.println(notificationPage2.getTravelId());
		}
		System.out.println("PAGE#3");
		for (Notification notificationPage3 : notificationsPage3) {
			System.out.println(notificationPage3.getTravelId());
		}
		System.out.println("PAGE#4");
		for (Notification notificationPage4 : notificationsPage4) {
			System.out.println(notificationPage4.getTravelId());
		}

		Assert.assertEquals(notificationsPage1.size(), 3);
		Assert.assertEquals(notificationsPage2.size(), 3);
		Assert.assertTrue(notificationsPage2.get(0).getTimestamp() < notificationsPage1.get(0).getTimestamp());
		Assert.assertEquals(notificationsPage3.size(), 3);
		Assert.assertTrue(notificationsPage3.get(0).getTimestamp() < notificationsPage2.get(0).getTimestamp());
		Assert.assertTrue(notificationsPage3.get(0).getTimestamp() < notificationsPage1.get(0).getTimestamp());
		Assert.assertEquals(notificationsPage4.size(), 3);
		Assert.assertTrue(notificationsPage4.get(0).getTimestamp() < notificationsPage3.get(0).getTimestamp());
		Assert.assertTrue(notificationsPage4.get(0).getTimestamp() < notificationsPage1.get(0).getTimestamp());
		
	}

	@Test
	public void testNotificationMarking() {

		// find all notifications of user.
		List<Notification> all = notificationRepository.findAllNotifications("53");
		// mark one notification as read.
		carPoolingManager.markNotification(all.get(0).getId());
		// find all read notification.
		List<Notification> readNotification = notificationRepository.findReadNotifications("53");
		// find all non read notification.
		List<Notification> nonReadNotification = notificationRepository.findUnReadNotifications("53");
		// total = read + non read.
		Assert.assertEquals(all.size(), (readNotification.size() + nonReadNotification.size()));

	}

}
