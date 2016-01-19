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

package it.smartcommunitylab.carpooling.managers;

import it.sayservice.platform.smartplanner.data.message.Itinerary;
import it.sayservice.platform.smartplanner.data.message.Position;
import it.sayservice.platform.smartplanner.data.message.RType;
import it.sayservice.platform.smartplanner.data.message.TType;
import it.sayservice.platform.smartplanner.data.message.journey.SingleJourney;
import it.smartcommunitylab.carpooling.model.Travel;
import it.smartcommunitylab.carpooling.model.Zone;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.mobilityservice.MobilityPlannerService;
import eu.trentorise.smartcampus.mobilityservice.MobilityServiceException;

@Component
public class MobilityPlanner {

	// mobility planner singleton.
	MobilityPlannerService mobilityPlannerService;
	// transport types.
	private TType[] tTypes = new TType[] { TType.CAR };
	// date formatter.
	SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mmaa", Locale.ITALY);
	Calendar calendar = Calendar.getInstance();
	// time formatter.

	@Autowired
	private Environment env;

	@PostConstruct()
	public void init() {
		if (mobilityPlannerService == null) {
			mobilityPlannerService = new MobilityPlannerService(env.getProperty("mobility.planner.url"));
		}

	}

	public List<Itinerary> plan(Zone fromZone, Zone toZone, Long when) {

		List<Itinerary> itns = new ArrayList<Itinerary>();

		SingleJourney singleJourney = new SingleJourney();
		singleJourney.setTransportTypes(tTypes);

		Position from = new Position();
		from.setName(from.getName());
		from.setLat(String.valueOf(fromZone.getLatitude()));
		from.setLon(String.valueOf(fromZone.getLongitude()));
		singleJourney.setFrom(from);

		Position to = new Position();
		to.setName(toZone.getName());
		to.setLat(String.valueOf(toZone.getLatitude()));
		to.setLon(String.valueOf(toZone.getLongitude()));
		singleJourney.setTo(to);

		String dateTimeString = formatter.format(new java.util.Date(when));

		String[] dateTime = dateTimeString.split("\\s");

		singleJourney.setDepartureTime(dateTime[1]);
		singleJourney.setDate(dateTime[0]);
		singleJourney.setResultsNumber(1);
		singleJourney.setRouteType(RType.fastest);

		try {
			itns = mobilityPlannerService.planSingleJourney(singleJourney, null);

		} catch (MobilityServiceException e) {
			e.printStackTrace();
		}

		return itns;
	}

}
