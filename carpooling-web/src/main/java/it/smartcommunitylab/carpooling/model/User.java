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

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.StringUtils;

import eu.trentorise.smartcampus.profileservice.model.BasicProfile;

@Document
public class User {

	@Id
	private String userId;
	/** name (reqd).**/
	private String name;
	/** surname (reqd).**/
	private String surname;
	/** email (reqd).**/
	private String email;
	/** orgins/destinations (reqd).**/
	private TravelProfile travelProfile;
	/** user points/ratings (reqd).**/
	private GameProfile gameProfile;
	/** description of car(if any).**/
	private Auto auto;
	/** number of offered travels. **/
	private int offeredTravels;
	/** number of participated travels. **/
	private int participatedTravels;

	public User() {
		super();
		// TODO Auto-generated constructor stub
	}

	public User(String userId, String name, String surname, String email, TravelProfile travelProfile,
			GameProfile gameProfile, Auto auto) {
		super();
		this.userId = userId;
		this.name = name;
		this.surname = surname;
		this.email = email;
		this.travelProfile = travelProfile;
		this.gameProfile = gameProfile;
		this.auto = auto;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public TravelProfile getTravelProfile() {
		return travelProfile;
	}

	public void setTravelProfile(TravelProfile travelProfile) {
		this.travelProfile = travelProfile;
	}

	public GameProfile getGameProfile() {
		return gameProfile;
	}

	public void setGameProfile(GameProfile gameProfile) {
		this.gameProfile = gameProfile;
	}

	public Auto getAuto() {
		return auto;
	}

	public void setAuto(Auto auto) {
		this.auto = auto;
	}
	
	public int getOfferedTravels() {
		return offeredTravels;
	}

	public void setOfferedTravels(int offeredTravels) {
		this.offeredTravels = offeredTravels;
	}

	public int getParticipatedTravels() {
		return participatedTravels;
	}

	public void setParticipatedTravels(int participatedTravels) {
		this.participatedTravels = participatedTravels;
	}

	/**
	 * @param basicProfile
	 * @return
	 */
	public static User fromUserProfile(BasicProfile basicProfile) {
		User user = new User();
		user.setName(basicProfile.getName());
		user.setSurname(basicProfile.getSurname());
		user.setUserId(basicProfile.getUserId());
		user.setTravelProfile(new TravelProfile());
		user.setGameProfile(new GameProfile());
		return user;
	}

	/**
	 * @return
	 */
	public String fullName() {
		String res = "";
		if (!StringUtils.isEmpty(name)) res += name.trim();
		if (!StringUtils.isEmpty(surname)) {
			if (res.length() > 0) res += " "; 
			res += surname.trim();
		}
		if (StringUtils.isEmpty(res)) return "Anonymous"+userId;
		return res;
	}

}
