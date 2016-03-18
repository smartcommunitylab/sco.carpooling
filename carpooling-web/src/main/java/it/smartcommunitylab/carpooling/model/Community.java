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

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 
 * @author nawazk.
 * 
 */

@Document
public class Community {

	@Id
	private String id;
	/** community name (reqd). **/
	private String name;
	/** list of user ids (reqd). **/
	private List<String> users = new ArrayList<String>();
	/** color. **/
	private String color;
	/** zone. **/
	private Zone zone;
	/** number of cars in community. **/
	private int cars;
	/** list of user objects. **/
	private List<User> userObjs = new ArrayList<User>();

	public Community() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Community(String id, String name, List<String> users) {
		super();
		this.id = id;
		this.name = name;
		this.users = users;
	}

	public Community(String id, String name, List<String> users, String color, Zone zone) {
		super();
		this.id = id;
		this.name = name;
		this.users = users;
		this.color = color;
		this.zone = zone;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public Zone getZone() {
		return zone;
	}

	public void setZone(Zone zone) {
		this.zone = zone;
	}

	public int getCars() {
		return cars;
	}

	public void setCars(int cars) {
		this.cars = cars;
	}

	public List<User> getUserObjs() {
		return userObjs;
	}

	public void setUserObjs(List<User> userObjs) {
		this.userObjs = userObjs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Community other = (Community) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	

}
