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

import java.util.Arrays;

import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;

/**
 * @author nawazk.
 */

public class Zone {

	private String name;
	/** either name or address cannot be null .**/
	private String address;
	/** latittude (reqd).**/
	private double latitude;
	/** longitude (reqd).**/
	private double longitude;
	/** zone radius(default value to understand).**/
	private double range;
//	@GeoSpatialIndexed
	private double[] coordinates;

	public Zone() {	super();
		// TODO Auto-generated constructor stub
	}

	public Zone(String name, String address, double latitude, double longitude, double range) {
		super();
		this.name = name;
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
		this.range = range;
		this.coordinates = new double[] { latitude, longitude };
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double[] getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(double[] coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public String toString() {
		return "Zone [name=" + name + ", address=" + address + ", latitude=" + latitude + ", longitude=" + longitude
				+ ", range=" + range + ", coordinates=" + Arrays.toString(coordinates) + "]";
	}

}
