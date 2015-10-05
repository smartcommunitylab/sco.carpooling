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

package it.smartcommunitylab.carpooling.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author nawazk
 *
 */
public class GameProfile {
	/** rating as of driver.**/
	private double driverRating;
	/** rating as of passenger.**/
	private double passengerRating;
	/** gamification points(key-value pairs for diff point types).**/
	private Map<String, Double> pointMap = new HashMap<String, Double>();
	/** ratings of other drivers.**/
	private Map<String, Integer> driverRatings = new HashMap<String, Integer>();
	/** ratings of other passengers.**/
	private Map<String, Integer> passengerRatings = new HashMap<String, Integer>();

	public GameProfile() {
		super();
		// TODO Auto-generated constructor stub
	}

	public GameProfile(double driverRating, double passengerRating, Map<String, Double> pointMap,
			Map<String, Integer> otherDriverRatings, Map<String, Integer> otherPassengerRatings) {
		super();
		this.driverRating = driverRating;
		this.passengerRating = passengerRating;
		this.pointMap = pointMap;
		this.driverRatings = otherDriverRatings;
		this.passengerRatings = otherPassengerRatings;
	}

	public double getDriverRating() {
		return driverRating;
	}

	public void setDriverRating(double driverRating) {
		this.driverRating = driverRating;
	}

	public double getPassengerRating() {
		return passengerRating;
	}

	public void setPassengerRating(double passengerRating) {
		this.passengerRating = passengerRating;
	}

	public Map<String, Double> getPointMap() {
		return pointMap;
	}

	public void setPointMap(Map<String, Double> pointMap) {
		this.pointMap = pointMap;
	}

	public Map<String, Integer> getDriverRatings() {
		return driverRatings;
	}

	public void setDriverRatings(Map<String, Integer> driverRatings) {
		this.driverRatings = driverRatings;
	}

	public Map<String, Integer> getPassengerRatings() {
		return passengerRatings;
	}

	public void setPassengerRatings(Map<String, Integer> passengerRatings) {
		this.passengerRatings = passengerRatings;
	}

}
