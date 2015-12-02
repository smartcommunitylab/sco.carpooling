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

package it.smartcommunitylab.carpooling.test.notification;

import it.smartcommunitylab.carpooling.notification.SendPushNotification;
import it.smartcommunitylab.carpooling.test.TestConfig;

import java.net.MalformedURLException;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@org.junit.Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
public class TestNotification {

	@Autowired
	private Environment env;
	@Autowired
	private SendPushNotification sendPushNotification;

	@Test
	public void testSendNotification() throws MalformedURLException, JSONException {

		String pushNotificationResult = sendPushNotification.sendNotification("parse.api.uri", "53", "alert",
				"Hi, a new trip offer is available matching your request");

		Assert.assertEquals(pushNotificationResult, "success");

	}
}
